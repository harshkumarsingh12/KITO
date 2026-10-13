package com.kito.feature.home

import com.kito.feature.home.data.HomeRepositoryImpl
import com.kito.testing.mockHttpClient
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeRepositoryImplTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private fun repo(rows: String, lottieStatus: HttpStatusCode = HttpStatusCode.OK, seenQuery: MutableList<String> = mutableListOf()) =
        HomeRepositoryImpl(mockHttpClient { req ->
            if (req.url.encodedPath.endsWith("events_and_ads")) {
                seenQuery += req.url.parameters["or"].orEmpty()
                respond(rows, HttpStatusCode.OK, json)
            } else {
                respond("""{"v":"5.5.7"}""", lottieStatus, json)
            }
        })

    private val imageRow = """{"id":1,"media_url":"https://cdn/img.jpg","media_type":"image","click_url":"https://x"}"""
    private val lottieRow = """{"id":2,"media_url":"https://cdn/banner.json","media_type":"lottie","click_url":"https://play"}"""

    @Test
    fun getEventsAndAds_filtersByPlatformOrNull() = runTest {
        val seen = mutableListOf<String>()
        repo("[$imageRow]", seenQuery = seen).getEventsAndAds("android")
        assertEquals(listOf("(platform.is.null,platform.eq.android)"), seen)
    }

    @Test
    fun getEventsAndAds_lottieRow_getsJson_imageRowUntouched() = runTest {
        val result = repo("[$imageRow,$lottieRow]").getEventsAndAds("ios").sortedBy { it.id }
        assertNull(result[0].lottieJson)
        assertEquals("""{"v":"5.5.7"}""", result[1].lottieJson)
    }

    @Test
    fun getEventsAndAds_lottieFetchFails_dropsOnlyThatRow() = runTest {
        val result = repo("[$imageRow,$lottieRow]", lottieStatus = HttpStatusCode.NotFound).getEventsAndAds("ios")
        assertEquals(listOf(1L), result.map { it.id })
    }
}
