package com.example.personaldocumentproject.presentation.capture

data class CaptureUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedImageUri: String? = null,
)
