package com.example.personaldocumentproject.presentation.setting

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.model.UserSetting
import com.example.personaldocumentproject.domain.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private  val repository: SettingRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settingFlow.collect { domainSettings ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isDarkMode = domainSettings.isDarkMode,
                        showImage = domainSettings.showImage,
                        widthValue = domainSettings.widthValue,
                        heightValue = domainSettings.heightValue
                    )
                }
            }
        }
    }

    fun toggleGeneralExpanded() {
        _uiState.update { it.copy(isGeneralExpanded = !it.isGeneralExpanded) }
    }

    fun toggleDocumentExpanded() {
        _uiState.update { it.copy(isDocumentExpanded = !it.isDocumentExpanded) }
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun setShowImage(show: Boolean) {
        _uiState.update { it.copy(showImage = show) }
    }

    fun setWidthValue(width: Float) {
        _uiState.update { it.copy(widthValue = width) }
    }

    fun setHeightValue(height: Float) {
        _uiState.update { it.copy(heightValue = height) }
    }

    fun applySetting() {
        viewModelScope.launch {
            val state = _uiState.value
            val userSetting = UserSetting(
                isDarkMode = state.isDarkMode,
                showImage = state.showImage,
                widthValue = state.widthValue,
                heightValue = state.heightValue
            )
            repository.saveSetting(userSetting)
        }
    }
}


