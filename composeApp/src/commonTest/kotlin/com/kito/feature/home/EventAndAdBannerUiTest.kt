package com.kito.feature.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import com.kito.feature.home.presentation.components.EventAndAdBanner
import com.kito.testing.eventOrAd
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EventAndAdBannerUiTest {

    @Test
    fun banner_lottieItem_renders() = runComposeUiTest {
        // auto-scroll and Lottie loop forever; freeze the clock so the test can settle
        mainClock.autoAdvance = false
        setContent {
            EventAndAdBanner(
                eventsAndAds = listOf(
                    eventOrAd(1L).copy(
                        mediaType = "lottie",
                        lottieJson = """{"v":"5.5.7","fr":30,"ip":0,"op":60,"w":1800,"h":300,"layers":[]}"""
                    )
                ),
                onClick = { _, _ -> }
            )
        }
        onNodeWithTag("events_banner").assertIsDisplayed()
    }
}
