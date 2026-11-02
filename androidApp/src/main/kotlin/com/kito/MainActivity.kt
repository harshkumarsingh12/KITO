package com.kito

import android.content.Intent
import java.io.File
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import androidx.navigation3.runtime.NavKey
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.kito.core.auth.AndroidAuthDeepLink
import com.kito.core.datastore.domain.repository.PrefsRepository
import com.kito.core.platform.ESP
import com.kito.core.platform.SecureStorage
import com.kito.core.presentation.navigation3.Routes
import com.kito.core.presentation.theme.KitoTheme
import com.kito.feature.app.presentation.MainUI
import com.kito.feature.home.presentation.HomeViewModel
import com.kito.feature.schedule.notification.NotificationPipelineController
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val prefs: PrefsRepository by inject()

    private val eSP: ESP by inject()

    private val secureStorage: SecureStorage by inject()

    private val notificationPipelineController by lazy {
        NotificationPipelineController.get(applicationContext)
    }
    private lateinit var appUpdateManager: AppUpdateManager

    private val updateLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            if (result.resultCode != RESULT_OK) {
                // User cancelled or update failed
            }
        }

    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showCompleteUpdateSnackbar()
        }
    }


    override fun onStart() {
        super.onStart()
        checkForUpdate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Start reading the saved timetable now, so Home's first frame already has it (no empty card).
        // Built off the main thread: resolving its dependency graph would otherwise delay the first frame.
        lifecycleScope.launch(Dispatchers.Default) { get<HomeViewModel>() }
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        appUpdateManager = if (!BuildConfig.DEBUG) {
            AppUpdateManagerFactory.create(this)
        }else{
            FakeAppUpdateManager(this)
        }
        if (appUpdateManager is FakeAppUpdateManager) {
            val fake = appUpdateManager as FakeAppUpdateManager
            fake.setUpdateAvailable(30)
            fake.setUpdatePriority(5)
        }
        setContent {
            var startDestination by remember { mutableStateOf<NavKey?>(null) }
            var deepLinkTarget by remember { mutableStateOf<String?>(null) }
            var isReady by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val intent = intent
                // Complete a Supabase OAuth redirect if the app was opened via kiito://auth-callback
                intent?.let { AndroidAuthDeepLink.handle(it) }
                if (intent?.data?.scheme == "kito" && intent.data?.host == "schedule") {
                    deepLinkTarget = "schedule"
                    // Clear intent to avoid re-triggering on rotation/re-entry
                    this@MainActivity.intent =
                        Intent(this@MainActivity, MainActivity::class.java)
                }
                // Legacy EncryptedSharedPreferences: opening it costs ~250ms of Keystore work while the
                // splash is up, so only touch it while its file exists, and delete it once migrated.
                if (File(applicationInfo.dataDir, "shared_prefs/secure_prefs.xml").exists()) {
                    val espPass = eSP.getSapPassword()
                    if (espPass.isEmpty() || secureStorage.saveSapPassword(espPass)) {
                        deleteSharedPreferences("secure_prefs")
                    }
                }
                val onboardingDone = prefs.onBoardingFlow.first()
                val isUserSetupDone = prefs.userSetupDoneFlow.first()
                startDestination = when {
                    !onboardingDone -> Routes.Onboarding
                    !isUserSetupDone -> Routes.UserSetup
                    else -> Routes.Tabs
                }
                isReady = true
                // Re-arming class alarms isn't needed for the first frame; do it after, off the main thread.
                lifecycleScope.launch(Dispatchers.Default) { notificationPipelineController.sync() }
            }

            // Handle new intents (e.g., if activity is singleTop)
            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { newIntent ->
                    AndroidAuthDeepLink.handle(newIntent)
                    if (newIntent.data?.scheme == "kito" && newIntent.data?.host == "schedule") {
                        deepLinkTarget = "schedule"
                        this@MainActivity.intent =
                            Intent(this@MainActivity, MainActivity::class.java)
                    }
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }
            splashScreen.setKeepOnScreenCondition { !isReady }
            if (isReady) {
                KitoTheme {
                    MainUI(
                        deepLinkTarget = deepLinkTarget,
                        onDeepLinkConsumed = { deepLinkTarget = null },
                        initialDestination = startDestination
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.registerListener(installStateListener)

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (
                info.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }



    override fun onPause() {
        super.onPause()
        appUpdateManager.unregisterListener(installStateListener)
    }

    private fun showCompleteUpdateSnackbar() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Update ready",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Restart") {
            appUpdateManager.completeUpdate()
        }.show()
    }
    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }
}