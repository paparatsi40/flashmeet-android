package com.carlitoswy.flashmeet.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.carlitoswy.flashmeet.domain.model.Event

/**
 * Abre Google Maps con la ubicación del evento.
 */
fun navigateWithGoogleMaps(context: Context, event: Event) {
    try {
        val gmmIntentUri =
            "geo:${event.latitude},${event.longitude}?q=${Uri.encode(event.title)}".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            Toast.makeText(context, "Google Maps no está instalado", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir Google Maps", Toast.LENGTH_LONG).show()
    }
}
