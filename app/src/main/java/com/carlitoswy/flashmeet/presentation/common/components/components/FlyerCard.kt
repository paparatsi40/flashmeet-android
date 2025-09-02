package com.carlitoswy.flashmeet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlitoswy.flashmeet.domain.model.Event

@Composable
fun FlyerCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    val bgColor = runCatching { Color(android.graphics.Color.parseColor(event.flyerBackgroundColor ?: "#FFFFFF")) }
        .getOrDefault(Color.White)

    val textColor = runCatching { Color(android.graphics.Color.parseColor(event.flyerTextColor ?: "#000000")) }
        .getOrDefault(Color.Black)

    val fontFamily = when (event.flyerFontFamily?.lowercase()) {
        "cursive" -> FontFamily.Cursive
        "monospace" -> FontFamily.Monospace
        "serif" -> FontFamily.Serif
        else -> FontFamily.SansSerif
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(color = bgColor, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = event.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = textColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = event.description,
                fontSize = 16.sp,
                fontFamily = fontFamily,
                fontStyle = FontStyle.Italic,
                color = textColor
            )
        }
    }
}
