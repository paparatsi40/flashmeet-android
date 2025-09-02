package com.carlitoswy.flashmeet.core.deeplink

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.Locale

/**
 * Builder de deeplinks/links de compartir.
 * - Genera https://{host}/event/{id} o la ruta corta /e/{id}
 * - Soporta lat/lon opcionales (?lat=..&lon=..)
 * - Formatea decimales con punto y evita "null" en URLs.
 * - Incluye helper para compartir con chooser.
 */
object DeeplinkBuilder {

    // 🌐 Hosts por defecto
    const val PROD_HOST = "123myway.com"
    const val PROD_HOST_WWW = "www.123myway.com"
    const val STAGING_HOST = "flashmeet-web.vercel.app"

    /**
     * Crea un link HTTPS del evento.
     * @param id       ID del evento
     * @param host     Host a usar (prod/staging)
     * @param shortPath true → usa /e/{id} ; false → /event/{id}
     * @param lat/lon  coordenadas opcionales
     */
    fun eventHttps(
        id: String,
        host: String = PROD_HOST,
        shortPath: Boolean = false,
        lat: Double? = null,
        lon: Double? = null,
        // ➕ tracking opcional:
        utmSource: String? = null,
        utmMedium: String? = null,
        utmCampaign: String? = null
    ): String {
        val basePath = if (shortPath) "e" else "event"
        val base = "https://$host/$basePath/${Uri.encode(id)}"
        val params = buildList {
            lat?.let { add("lat=${it.asParam()}") }
            lon?.let { add("lon=${it.asParam()}") }
            utmSource?.let   { add("utm_source=${Uri.encode(it)}") }
            utmMedium?.let   { add("utm_medium=${Uri.encode(it)}") }
            utmCampaign?.let { add("utm_campaign=${Uri.encode(it)}") }
        }
        return if (params.isEmpty()) base else "$base?${params.joinToString("&")}"
    }

    /** Crea el custom scheme (fallback): flashmeet://event/{id} */
    fun eventCustomScheme(id: String): String =
        "flashmeet://event/${Uri.encode(id)}"

    /** Intent listo para compartir por cualquier app. */
    fun shareEventIntent(
        id: String,
        host: String = PROD_HOST,
        shortPath: Boolean = false,
        lat: Double? = null,
        lon: Double? = null
    ): Intent {
        val url = eventHttps(id, host, shortPath, lat, lon)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
    }

    /** Abre chooser para compartir el link del evento. */
    fun shareEvent(
        context: Context,
        id: String,
        host: String = PROD_HOST,
        shortPath: Boolean = false,
        lat: Double? = null,
        lon: Double? = null,
        chooserTitle: String = "Compartir evento"
    ) {
        val intent = shareEventIntent(id, host, shortPath, lat, lon)
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    // --- Helpers privados ---
    private fun Double.asParam(decimals: Int = 6): String =
        String.format(Locale.US, "%.${decimals}f", this)
}
