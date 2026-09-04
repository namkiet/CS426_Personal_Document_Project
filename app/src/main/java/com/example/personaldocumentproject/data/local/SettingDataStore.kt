package com.example.personaldocumentproject.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.personaldocumentproject.domain.model.UserSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Top-level extension property ensures a single instance across the app
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

// Preference Keys
object SettingsKeys {
    val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    val DISABLE_API_MODE = booleanPreferencesKey("disable_api_mode")
    val START_CAMERA_MODE = booleanPreferencesKey("start_camera_mode")
}

class SettingsLocalDataSource(private val dataStore: DataStore<Preferences>) {

    val userSettingFlow: Flow<UserSetting> = dataStore.data.map { preferences ->
        UserSetting(
            isDarkMode = preferences[SettingsKeys.IS_DARK_MODE] ?: false,
            disableApiMode = preferences[SettingsKeys.DISABLE_API_MODE] ?: false,
            startCameraMode = preferences[SettingsKeys.START_CAMERA_MODE] ?: false,
        )
    }

    suspend fun saveUserSettings(settings: UserSetting) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.IS_DARK_MODE] = settings.isDarkMode
            preferences[SettingsKeys.DISABLE_API_MODE] = settings.disableApiMode
            preferences[SettingsKeys.START_CAMERA_MODE] = settings.startCameraMode
        }
    }

    suspend fun clearSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}