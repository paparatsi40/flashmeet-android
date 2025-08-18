package com.carlitoswy.flashmeet.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.datastore.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> get() = _isDarkTheme

    init {
        viewModelScope.launch {
            ThemePreferences.isDarkMode(context).collect {
                _isDarkTheme.value = it
            }
        }
    }

    fun toggleTheme(enabled: Boolean) {
        viewModelScope.launch {
            ThemePreferences.setDarkMode(context, enabled)
        }
    }
}
