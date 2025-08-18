package com.carlitoswy.flashmeet.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.carlitoswy.flashmeet.datastore.DataStoreModule.dataStore

class LanguagePreferenceManager(private val context: Context) {

    companion object {
        private val LANGUAGE_CODE = stringPreferencesKey("language_code")
        private const val DEFAULT_LANGUAGE = "es" // Idioma por defecto si no hay selección
    }

    suspend fun saveLanguageCode(langCode: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_CODE] = langCode
        }
    }

    suspend fun getLanguageCode(): String {
        val flow = context.dataStore.data.map { prefs ->
            prefs[LANGUAGE_CODE] ?: DEFAULT_LANGUAGE
        }
        return flow.first()
    }
}
