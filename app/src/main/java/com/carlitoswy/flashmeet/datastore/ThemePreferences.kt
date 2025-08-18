package com.carlitoswy.flashmeet.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

object ThemePreferences {
    private val THEME_KEY = booleanPreferencesKey("dark_mode_enabled")

    fun isDarkMode(context: Context): Flow<Boolean> {
        return context.themeDataStore.data.map { prefs ->
            prefs[THEME_KEY] ?: false
        }
    }

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[THEME_KEY] = enabled
        }
    }
}
