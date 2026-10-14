package com.kito.feature.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first

@Composable
fun LottieBannerImage(
    json: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    onFinished: () -> Unit = {},
) {
    val composition by rememberLottieComposition(json) {
        LottieCompositionSpec.JsonString(json)
    }
    // One play per turn; restartOnPlay rewinds to frame 0 each time the page becomes current again.
    val state = animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = true,
        iterations = 1
    )
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        // isAtEnd is still true from the previous turn until the rewind lands; skip that, then wait for the real end.
        snapshotFlow { state.isAtEnd }.dropWhile { it }.first { it }
        onFinished()
    }
    val painter = rememberLottiePainter(
        composition = composition,
        progress = { state.progress }
    )
    if (composition != null) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
