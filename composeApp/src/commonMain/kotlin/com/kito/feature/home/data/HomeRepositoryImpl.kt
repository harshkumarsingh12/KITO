package com.kito.feature.home.data

import com.kito.core.network.supabase.model.EventAndAdModel
import com.kito.core.network.supabase.model.FeatureFlagModel
import com.kito.feature.home.data.mapper.toDomain
import com.kito.feature.home.domain.model.EventOrAd
import com.kito.feature.home.domain.repository.HomeRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

import org.koin.core.annotation.Provided

class HomeRepositoryImpl(
    @Provided private val client: HttpClient,
) : HomeRepository {

    override suspend fun getEventsAndAds(platform: String): List<EventOrAd> {
        val response = client.get("rest/v1/events_and_ads") {
            parameter("is_active", "eq.true")
            // null platform = shown on both
            parameter("or", "(platform.is.null,platform.eq.$platform)")
            parameter("order", "display_order.asc")
        }.body<List<EventAndAdModel>>()
        return response.map { it.toDomain() }
            .mapNotNull { if (it.mediaType == "lottie") it.withLottieJson() else it }
            .shuffled()
    }

    // Lottie rows carry the JSON's URL; a row whose JSON can't be fetched is dropped instead of showing a blank card.
    private suspend fun EventOrAd.withLottieJson(): EventOrAd? = runCatching {
        val res = client.get(mediaUrl)
        if (res.status.isSuccess()) copy(lottieJson = res.bodyAsText()) else null
    }.getOrNull()

    override suspend fun getFeatureFlags(): Map<String, Boolean> =
        client.get("rest/v1/feature_flag").body<List<FeatureFlagModel>>()
            .associate { it.feature_name to it.isEnabled }
}
