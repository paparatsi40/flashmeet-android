// app/src/main/java/com.carlitoswy.flashmeet.utils/LocaleManager.kt
package com.carlitoswy.flashmeet.localization

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

object LocaleManager {

    fun applyLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    // <-- AÑADE ESTA FUNCIÓN A TU LocaleManager -->
    fun setAndSaveLocale(context: Context, language: String) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit {
                putString("language", language)
            }
    }

    // <-- Probablemente ya la tienes, si no, añádela también -->
    fun getSavedLanguage(context: Context): String {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("language", "es") ?: "es"
    }
}
