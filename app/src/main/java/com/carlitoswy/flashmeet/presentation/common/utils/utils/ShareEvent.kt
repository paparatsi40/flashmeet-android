package com.carlitoswy.flashmeet.utils

import android.content.Context
import android.content.Intent
import com.carlitoswy.flashmeet.domain.model.Event

/**
 * 🔥 Utilidad para compartir un evento con otras apps
 */
fun shareEvent(context: Context, event: Event) {
    val shareText = buildString {
        append("🎉 *${event.title}* \n")
        append("${event.description}\n\n")
        append("📍 Ubicación: ${event.locationName}\n")
        append("🌍 Lat: ${event.latitude}, Lng: ${event.longitude}\n\n")
        append("✨ ¡Descúbrelo en FlashMeet! ✨")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Evento: ${event.title}")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    context.startActivity(Intent.createChooser(intent, "Compartir evento con:"))
}
