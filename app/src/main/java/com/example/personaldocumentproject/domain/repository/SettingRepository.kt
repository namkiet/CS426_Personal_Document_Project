package com.example.personaldocumentproject.domain.repository

import com.example.personaldocumentproject.domain.model.UserSetting
import kotlinx.coroutines.flow.Flow

interface SettingRepository {
    val settingFlow: Flow<UserSetting>
    suspend fun saveSetting(setting: UserSetting)
}