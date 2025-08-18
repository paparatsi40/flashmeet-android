package com.carlitoswy.flashmeet.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun SplashAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("splash_animation.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1.0f
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(size)
    )
}

@Composable
fun WelcomeAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 300.dp
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.carlitoswy.flashmeet.R.raw.welcome_animation)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever, // ✅ Ahora se repite
        speed = 1.0f,
        restartOnPlay = true
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(size),
        enableMergePaths = true
    )
}


@Composable
fun RegisterAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("register_anim.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(size)
    )
}
