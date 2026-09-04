package com.example.personaldocumentproject.data.repository

import com.example.personaldocumentproject.data.local.SettingsLocalDataSource
import com.example.personaldocumentproject.domain.model.UserSetting
import com.example.personaldocumentproject.domain.repository.SettingRepository
import kotlinx.coroutines.flow.Flow

class SettingRepositoryImpl(
    private val localDataSource: SettingsLocalDataSource
) : SettingRepository {

    override val settingFlow: Flow<UserSetting> = localDataSource.userSettingFlow

    override suspend fun saveSetting(setting: UserSetting) {
        localDataSource.saveUserSettings(setting)
    }

    override suspend fun clearSettings() {
        localDataSource.clearSettings()
    }
}