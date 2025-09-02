package com.carlitoswy.flashmeet.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.carlitoswy.flashmeet.R
import androidx.compose.ui.Alignment

@Composable
fun RadarUltraEffect(
    modifier: Modifier = Modifier.size(150.dp),
    zoomLevel: Float = 14f,
    baseColor: Color = Color.Cyan,
    waveColor: Color = Color.Blue.copy(alpha = 0.2f),
    alertActive: Boolean = false, // 🌟 NUEVO: estado de alerta
    iconRes: Int = R.drawable.ic_flashmeet_logo,
    iconSize: Dp = 48.dp
) {
    val transition = rememberInfiniteTransition()

    val scaleFactor = (1.5f - (zoomLevel / 20f)).coerceIn(0.4f, 1.2f)
    val waveSpeed = (3000 - (zoomLevel * 100)).toInt().coerceIn(1200, 3000)
    val pulseSpeed = (1200 - (zoomLevel * 50)).toInt().coerceIn(600, 1200)

    // 🌟 Ondas sonar normales
    val waveRadius by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.2f * scaleFactor,
        animationSpec = infiniteRepeatable(tween(waveSpeed), RepeatMode.Restart)
    )

    // 🌟 Pulso normal
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f * scaleFactor,
        animationSpec = infiniteRepeatable(tween(pulseSpeed), RepeatMode.Reverse)
    )

    // 🚨 Parpadeo ALERTA: solo si alertActive == true
    val alertAlpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (alertActive) 1f else 0.2f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse)
    )

    Box(modifier = modifier) {
        // ✅ Onda normal
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(baseColor.copy(alpha = alertAlpha), waveColor.copy(alpha = 0f)),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = (size.minDimension / 2) * waveRadius
                )
            )
        }

        // ✅ Icono con pulso
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Radar LIVE Icon",
            modifier = Modifier
                .size(iconSize)
                .align(Alignment.Center)
                .scale(pulseScale)
        )
    }
}

