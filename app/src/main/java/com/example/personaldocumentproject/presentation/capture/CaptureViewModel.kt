package com.example.personaldocumentproject.presentation.capture

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CaptureViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(CaptureUiState())
    val uiState = mutableState.asStateFlow()
    fun selectImage(uri: String) { mutableState.value = CaptureUiState(uri) }
    fun onImageSelectionHandled() { mutableState.value = CaptureUiState(null) }
}
