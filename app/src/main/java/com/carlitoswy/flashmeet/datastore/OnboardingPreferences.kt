package com.carlitoswy.flashmeet.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("onboarding_prefs")

object OnboardingPrefs {
    private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")

    suspend fun hasSeenOnboarding(context: Context): Boolean {
        return context.dataStore.data.map { prefs ->
            prefs[HAS_SEEN_ONBOARDING] ?: false
        }.first()
    }

    // ✨ CAMBIO AQUÍ: Renombramos a 'setHasSeenOnboarding' y le añadimos un parámetro 'hasSeen'
    suspend fun setHasSeenOnboarding(context: Context, hasSeen: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAS_SEEN_ONBOARDING] = hasSeen // Ahora podemos guardar true o false
        }
    }
}
