package com.example.personaldocumentproject.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.model.UserSetting
import com.example.personaldocumentproject.domain.repository.DocumentRepository
import com.example.personaldocumentproject.domain.repository.SettingRepository
import com.example.personaldocumentproject.domain.service.ImageStorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val repository: SettingRepository,
    private val documentRepository: DocumentRepository? = null,
    private val imageStorageService: ImageStorageService? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settingFlow.collect { domainSettings ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isDarkMode = domainSettings.isDarkMode,
                        disableApiMode = domainSettings.disableApiMode,
                        startCameraMode = domainSettings.startCameraMode,
                    )
                }
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
        saveCurrentSettings()
    }

    fun setDisableApiMode(disabled: Boolean) {
        _uiState.update { it.copy(disableApiMode = disabled) }
        saveCurrentSettings()
    }

    fun setStartCameraMode(enabled: Boolean) {
        _uiState.update { it.copy(startCameraMode = enabled) }
        saveCurrentSettings()
    }

    fun showResetDialog(show: Boolean) {
        _uiState.update { it.copy(showResetDialog = show) }
    }

    fun performFactoryReset() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResetting = true, showResetDialog = false) }
            try {
                documentRepository?.deleteAll()
                imageStorageService?.clearAllImages()
                repository.clearSettings()
                _uiState.update {
                    it.copy(
                        isResetting = false,
                        resetMessage = "All data and settings have been reset successfully.",
                        isDarkMode = false,
                        disableApiMode = false,
                        startCameraMode = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isResetting = false,
                        resetMessage = "Reset failed: ${e.message}",
                    )
                }
            }
        }
    }

    fun clearResetMessage() {
        _uiState.update { it.copy(resetMessage = null) }
    }

    private fun saveCurrentSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            val userSetting = UserSetting(
                isDarkMode = state.isDarkMode,
                disableApiMode = state.disableApiMode,
                startCameraMode = state.startCameraMode,
            )
            repository.saveSetting(userSetting)
        }
    }
}


