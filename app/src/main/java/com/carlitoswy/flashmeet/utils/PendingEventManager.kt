package com.carlitoswy.flashmeet.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ✅ PendingEventManager híbrido:
 *  - Guarda eventId en SharedPreferences para sobrevivir Cold Start.
 *  - Expone un StateFlow para notificar cambios en caliente a la UI.
 */
object PendingEventManager {

    private const val PREFS_NAME = "pending_events_prefs"
    private const val KEY_PENDING_EVENT_ID = "pending_event_id"

    private val _pendingEventId = MutableStateFlow<String?>(null)
    val pendingEventId: StateFlow<String?> get() = _pendingEventId

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** 🔥 Guarda el último eventId recibido y emite en StateFlow */
    fun savePendingEvent(context: Context, eventId: String) {
        getPrefs(context).edit().putString(KEY_PENDING_EVENT_ID, eventId).apply()
        _pendingEventId.value = eventId
    }

    /** ✅ Obtiene el último eventId pendiente desde prefs y lo borra */
    fun consumePendingEvent(context: Context): String? {
        val prefs = getPrefs(context)
        val eventId = prefs.getString(KEY_PENDING_EVENT_ID, null)
        prefs.edit().remove(KEY_PENDING_EVENT_ID).apply()
        if (eventId != null) _pendingEventId.value = null
        return eventId
    }

    /** ✅ Carga el valor persistente al iniciar la app */
    fun loadPendingEvent(context: Context) {
        val eventId = getPrefs(context).getString(KEY_PENDING_EVENT_ID, null)
        _pendingEventId.value = eventId
    }

    /** ✅ Limpia manualmente cualquier evento pendiente */
    fun clearPendingEvent(context: Context) {
        getPrefs(context).edit().remove(KEY_PENDING_EVENT_ID).apply()
        _pendingEventId.value = null
    }
}
