// ui/theme/Theme.kt
package com.carlitoswy.flashmeet.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color


private val DarkColorScheme = darkColorScheme(
    primary = FlashPrimary,
    secondary = FlashSecondary,
    tertiary = FlashTertiary,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = FlashOnPrimary,
    onSecondary = FlashOnSecondary,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = FlashPrimary,
    secondary = FlashSecondary,
    tertiary = FlashTertiary,
    background = FlashBackground,
    surface = FlashSurface,
    onPrimary = FlashOnPrimary,
    onSecondary = FlashOnSecondary,
    onBackground = FlashOnBackground,
    onSurface = FlashOnSurface
)

@Composable
fun FlashMeetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
