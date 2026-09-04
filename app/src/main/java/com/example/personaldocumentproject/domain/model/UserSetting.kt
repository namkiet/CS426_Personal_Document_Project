package com.example.personaldocumentproject.domain.model

data class UserSetting(
    val isDarkMode: Boolean = false,
    val showImage: Boolean = true,
    val widthValue: Float = 0.1f,
    val heightValue: Float = 0.1f
)