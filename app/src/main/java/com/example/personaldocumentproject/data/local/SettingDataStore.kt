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
    val SHOW_IMAGE = booleanPreferencesKey("show_image")
    val WIDTH_VALUE = floatPreferencesKey("width_value")
    val HEIGHT_VALUE = floatPreferencesKey("height_value")
}

class SettingsLocalDataSource(private val dataStore: DataStore<Preferences>) {

    val userSettingFlow: Flow<UserSetting> = dataStore.data.map { preferences ->
        UserSetting(
            isDarkMode = preferences[SettingsKeys.IS_DARK_MODE] ?: false,
            showImage = preferences[SettingsKeys.SHOW_IMAGE] ?: true,
            widthValue = preferences[SettingsKeys.WIDTH_VALUE] ?: 0.1f,
            heightValue = preferences[SettingsKeys.HEIGHT_VALUE] ?: 0.1f
        )
    }

    suspend fun saveUserSettings(settings: UserSetting) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.IS_DARK_MODE] = settings.isDarkMode
            preferences[SettingsKeys.SHOW_IMAGE] = settings.showImage
            preferences[SettingsKeys.WIDTH_VALUE] = settings.widthValue
            preferences[SettingsKeys.HEIGHT_VALUE] = settings.heightValue
        }
    }
}