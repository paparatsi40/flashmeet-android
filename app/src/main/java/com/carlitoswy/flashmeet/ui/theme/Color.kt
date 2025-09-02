// ui/theme/Color.kt
package com.carlitoswy.flashmeet.ui.theme

import androidx.compose.ui.graphics.Color

val FlashPrimary = Color(0xFF0077CC)
val FlashSecondary = Color(0xFF00C896)
val FlashTertiary = Color(0xFFFFC107)

val FlashBackground = Color(0xFFF2F2F2)
val FlashSurface = Color.White
val FlashOnPrimary = Color.White
val FlashOnSecondary = Color.Black
val FlashOnBackground = Color.Black
val FlashOnSurface = Color.Black

fun Color.toHex(): String {
    val red = (red * 255).toInt().coerceIn(0, 255)
    val green = (green * 255).toInt().coerceIn(0, 255)
    val blue = (blue * 255).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", red, green, blue)
}
