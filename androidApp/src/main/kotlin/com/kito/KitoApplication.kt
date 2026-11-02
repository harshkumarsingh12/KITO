package com.kito

import android.app.Application
import com.kito.core.di.initKoin
import com.kito.core.platform.AppConfig
import com.kito.core.platform.PlatformContext
import com.kito.feature.schedule.notification.createClassNotificationChannel

class KitoApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Before anything can build a client: MainActivity preloads HomeViewModel (network calls in init),
        // and widgets/workers/receivers start without an Activity at all.
        AppConfig.init(
            portalBase = BuildConfig.PORTAL_BASE,
            wdPath = BuildConfig.WD_PATH,
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY,
            isDebug = BuildConfig.DEBUG,
            kgAPIKey = BuildConfig.KG_API_KEY,
            kgBaseURL = BuildConfig.KG_BASE_URL,
            cdnURL = BuildConfig.CDN_URL,
            googleServerClientId = BuildConfig.GOOGLE_SERVER_CLIENT_ID
        )
        PlatformContext.init(this)
        initKoin(this)
        createClassNotificationChannel(this)

    }
}
