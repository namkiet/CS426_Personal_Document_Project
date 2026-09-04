package com.example.personaldocumentproject.presentation.setting

data class SettingUiState(
    val isGeneralExpanded: Boolean = true,
    val isDocumentExpanded: Boolean = true,
    val isDarkMode: Boolean = false,
    val showImage: Boolean = true,
    val widthValue: Float = 0.1f,
    val heightValue: Float = 0.1f
)