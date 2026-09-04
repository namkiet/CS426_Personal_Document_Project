package com.example.personaldocumentproject


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.model.UserSetting
import com.example.personaldocumentproject.domain.repository.SettingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    settingsRepository: SettingRepository
) : ViewModel() {

    // Map domain settings directly to a dark theme boolean state
    val userSetting: StateFlow<UserSetting> = settingsRepository.settingFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSetting()
        )
}