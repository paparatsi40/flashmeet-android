package com.carlitoswy.flashmeet.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

object DataStoreModule {
    val Context.dataStore by preferencesDataStore(name = "flashmeet_preferences")
}
