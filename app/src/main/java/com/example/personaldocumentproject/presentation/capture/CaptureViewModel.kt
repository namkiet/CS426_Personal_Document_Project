package com.example.personaldocumentproject.presentation.capture

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CaptureViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(CaptureUiState())
    val uiState = mutableState.asStateFlow()

    fun selectImage(uri: String) {
        mutableState.update { it.copy(selectedImageUri = uri, isLoading = false, errorMessage = null) }
    }

    fun onImageSelectionHandled() {
        mutableState.update { it.copy(selectedImageUri = null) }
    }

    fun setLoading(loading: Boolean) {
        mutableState.update { it.copy(isLoading = loading) }
    }

    fun setError(message: String?) {
        mutableState.update { it.copy(errorMessage = message, isLoading = false) }
    }

    fun clearError() {
        mutableState.update { it.copy(errorMessage = null) }
    }
}
