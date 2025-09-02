package com.carlitoswy.flashmeet.ui.navigation

import android.net.Uri
import java.util.Locale

object Routes {

    // 🔹 Helper para editar eventos
    fun editEventWithId(eventId: String) = "$EDIT_EVENT/$eventId"
    fun eventDetailWithId(eventId: String) = "$EVENT_DETAIL/$eventId"

    // 🔹 Flujo inicial
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val ONBOARDING = "onboarding"

    // 🔹 Contenedor de autenticación
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val LOGIN_EMAIL = "login_email"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    // 🔹 Pantallas principales
    const val HOME = "home"
    const val MAP = "map"
    const val PERMISSION = "permission"

    // 👉 Ruta con foco opcional (para deeplink → centrar mapa + abrir detalle)
    // Declaración del destino con placeholders opcionales:
    //   route = "home?focusId={focusId}&lat={lat}&lon={lon}"
    // y navArguments con defaultValue = null (como ya tienes).
    const val HOME_FOCUS = "home?focusId={focusId}&lat={lat}&lon={lon}"

    /**
     * ✅ Builder seguro para navegar a HOME (con o sin foco y coords).
     * - Omite params nulos/vacíos.
     * - Uri.encode en focusId.
     * - Decimales con punto (Locale.US).
     *
     * Ejemplos:
     *  homeWithFocus(null)                     -> "home"
     *  homeWithFocus("abc")                    -> "home?focusId=abc"
     *  homeWithFocus("abc", -34.6, -58.4)      -> "home?focusId=abc&lat=-34.600000&lon=-58.400000"
     */
    fun homeWithFocus(
        eventId: String?,
        lat: Double? = null,
        lon: Double? = null
    ): String {
        val params = buildList {
            eventId?.takeIf { it.isNotBlank() }?.let { add("focusId=${Uri.encode(it)}") }
            lat?.let { add("lat=${it.asParam()}") }
            lon?.let { add("lon=${it.asParam()}") }
        }
        return if (params.isEmpty()) HOME else "$HOME?${params.joinToString("&")}"
    }

    // 🔹 Eventos
    const val CREATE_EVENT = "create_event"
    const val CAMERA = "camera_capture"
    const val MY_EVENTS = "my_events"
    const val FAVORITES = "favorites"
    const val EVENT_DETAIL = "event_detail"
    const val EDIT_EVENT = "edit_event"

    // 👉 Ruta para deeplink directo a evento (se usa en NavGraph con deepLinks)
    const val EVENT = "event/{eventId}"
    fun event(eventId: String) = "event/${Uri.encode(eventId)}"

    // 🔹 Flyers
    const val FLYER_EDITOR = "flyer_editor"
    const val FLYER_LIST = "flyer_list"
    const val CREATE_FLYER = "create_flyer"

    // 🔹 Otros
    const val PHOTO_PREVIEW = "photo_preview"
    const val PAYMENT = "payment"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val SEARCH_NEARBY = "search_nearby"
}

    // --- Helpers privados ---

    /** Formatea Double con punto decimal y 6 decimales (ajusta si quieres). */
    private fun Double.asParam(decimals: Int = 6): String =
        String.format(Locale.US, "%.${decimals}f", this)

