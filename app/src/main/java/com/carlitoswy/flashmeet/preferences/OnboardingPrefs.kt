package com.carlitoswy.flashmeet.preferences

import android.content.Context

object OnboardingPrefs {
    private const val PREF_NAME = "onboarding"
    private const val KEY_SEEN = "seen"

    fun hasSeen(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(KEY_SEEN, false)
    }

    fun setSeen(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean(KEY_SEEN, true).apply()
    }
}
