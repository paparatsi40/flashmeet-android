package com.carlitoswy.flashmeet.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale

@Composable
fun RadarWaves(
    modifier: Modifier = Modifier,
    color: Color = Color.Cyan,
    waveCount: Int = 3,
    baseAlpha: Float = 0.4f,
    duration: Int = 3000
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Animación principal: ondas que se expanden
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        repeat(waveCount) { i ->
            val delayFactor = i * 0.2f
            val alpha = (baseAlpha - (i * 0.1f)).coerceAtLeast(0f)

            scale(scaleAnim + delayFactor) {
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = size.minDimension / 3
                )
            }
        }
    }
}
