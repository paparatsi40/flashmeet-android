package com.carlitoswy.flashmeet.utils

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

object LocaleManager {

    private const val PREFS_NAME = "settings"
    private const val LANGUAGE_KEY = "language"
    private const val DEFAULT_LANG = "es" // Español como fallback

    /**
     * Aplica el idioma actual guardado al contexto.
     * Usado principalmente desde attachBaseContext.
     */
    fun applyLocale(context: Context): Context {
        val language = getPersistedLanguage(context)
        return applyLocale(context, language)
    }

    /**
     * Aplica un idioma específico al contexto dado.
     */
    fun applyLocale(context: Context, language: String): Context {
        persistLanguage(context, language) // Guarda siempre que se llame
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Guarda el idioma en SharedPreferences.
     */
    fun setAndSaveLocale(context: Context, language: String) {
        applyLocale(context, language)
        // Si necesitas forzar recreate() lo haces en la Activity después
    }

    /**
     * Obtiene el idioma guardado (o "es" si no hay nada).
     */
    fun getPersistedLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, DEFAULT_LANG) ?: DEFAULT_LANG
    }

    /**
     * Guarda el idioma sin aplicar (por si quieres controlarlo aparte).
     */
    private fun persistLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(LANGUAGE_KEY, language)
            }
    }
}
