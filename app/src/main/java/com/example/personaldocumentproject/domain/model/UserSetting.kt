package com.example.personaldocumentproject.domain.model

data class UserSetting(
    val isDarkMode: Boolean = false,
    val disableApiMode: Boolean = false,
    val startCameraMode: Boolean = false,
    val isAutoCapture: Boolean = true,
)