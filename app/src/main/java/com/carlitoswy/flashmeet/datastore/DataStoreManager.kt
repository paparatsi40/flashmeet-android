package com.carlitoswy.flashmeet.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
    }

    // Save values
    suspend fun setIsLoggedIn(value: Boolean) {
        context.dataStore.edit { it[IS_LOGGED_IN] = value }
    }

    suspend fun saveUserData(name: String, email: String, photoUrl: String?) {
        context.dataStore.edit {
            it[USER_NAME] = name
            it[USER_EMAIL] = email
            if (photoUrl != null) it[USER_PHOTO_URL] = photoUrl
        }
    }

    // Read values
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data
        .map { it[IS_LOGGED_IN] ?: false }

    val userNameFlow: Flow<String?> = context.dataStore.data
        .map { it[USER_NAME] }

    val userEmailFlow: Flow<String?> = context.dataStore.data
        .map { it[USER_EMAIL] }

    val userPhotoFlow: Flow<String?> = context.dataStore.data
        .map { it[USER_PHOTO_URL] }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
