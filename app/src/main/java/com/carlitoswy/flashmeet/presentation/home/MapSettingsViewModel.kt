package com.carlitoswy.flashmeet.presentation.home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.mapDataStore: DataStore<Preferences> by preferencesDataStore(name = "map_settings")

@HiltViewModel
class MapSettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private object Keys {
        val DARK_MAP = booleanPreferencesKey("dark_map")
    }

    val darkMap = appContext.mapDataStore.data
        .map { it[Keys.DARK_MAP] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleDarkMap() {
        viewModelScope.launch {
            appContext.mapDataStore.edit { prefs ->
                val current = prefs[Keys.DARK_MAP] ?: false
                prefs[Keys.DARK_MAP] = !current
            }
        }
    }
}
