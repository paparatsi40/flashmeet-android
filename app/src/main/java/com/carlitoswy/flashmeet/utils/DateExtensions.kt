package com.carlitoswy.flashmeet.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extensión para formatear timestamps (en milisegundos) a un formato legible.
 * Puedes modificar el patrón según tus necesidades.
 */
fun Long.dateFormatted(pattern: String = "dd MMM yyyy, HH:mm"): String {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.format(Date(this))
    } catch (e: Exception) {
        "" // Devuelve vacío en caso de error
    }
}
