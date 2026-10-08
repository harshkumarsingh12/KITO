package com.kito.feature.home

import com.kito.testing.FakeHomeRepository
import com.kito.testing.eventOrAd
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeRepositoryTest {

    @Test
    fun getEventsAndAds_returnsList() = runTest {
        val repo = FakeHomeRepository(events = listOf(eventOrAd(1L), eventOrAd(2L, isAd = true)))
        val result = repo.getEventsAndAds()
        assertEquals(2, result.size)
        assertTrue(result[1].isAd)
    }

    @Test
    fun getFeatureFlags_defaultsEmpty() = runTest {
        assertTrue(FakeHomeRepository().getFeatureFlags().isEmpty())
    }

    @Test
    fun getFeatureFlags_whenSet_returnsFlags() = runTest {
        val flags = mapOf("banner" to true, "utilities" to false)
        assertEquals(flags, FakeHomeRepository(featureFlags = flags).getFeatureFlags())
    }
}
