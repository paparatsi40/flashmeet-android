package com.carlitoswy.flashmeet.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_SHOW_LIST = booleanPreferencesKey("show_list_screen")
    }

    val showListFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_LIST] ?: false
    }

    suspend fun setShowListScreen(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_LIST] = show
        }
    }
}
