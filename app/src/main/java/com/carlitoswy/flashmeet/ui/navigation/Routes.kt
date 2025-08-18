package com.carlitoswy.flashmeet.ui.navigation

object Routes {

    // 🔹 Helper para editar eventos
    fun editEventWithId(eventId: String) = "$EDIT_EVENT/$eventId"

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
    // Ejemplo real de navegación: home?focusId=123&lat=-12.06&lon=-77.04
    const val HOME_FOCUS = "home?focusId={focusId}&lat={lat}&lon={lon}"

    // Helper para construir la ruta con foco
    fun homeWithFocus(eventId: String, lat: Double, lon: Double): String =
        "home?focusId=$eventId&lat=$lat&lon=$lon"

    // 🔹 Eventos
    const val CREATE_EVENT = "create_event"
    const val MY_EVENTS = "my_events"
    const val FAVORITES = "favorites"
    const val EVENT_DETAIL = "event_detail"
    const val EDIT_EVENT = "edit_event"

    // 👉 Ruta para deeplink directo a evento (se usa en NavGraph con deepLinks)
    // Ejemplo de match: https://flashmeet.app/e/{eventId} o flashmeet://event/{eventId}
    const val EVENT = "event/{eventId}"
    fun event(eventId: String) = "event/$eventId"

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
}
