package com.example.personaldocumentproject.presentation.setting

data class SettingUiState(
    val isDarkMode: Boolean = false,
    val disableApiMode: Boolean = false,
    val startCameraMode: Boolean = false,
    val showResetDialog: Boolean = false,
    val isResetting: Boolean = false,
    val resetMessage: String? = null,
)