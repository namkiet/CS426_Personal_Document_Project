package com.example.personaldocumentproject.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.usecase.GetDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val getDocument: GetDocument
) : ViewModel() {
    private val mutableState = MutableStateFlow(DetailUiState())
    val uiState = mutableState.asStateFlow()

    fun loadDocument(documentId: String) {
        if (mutableState.value.document?.id == documentId) return

        viewModelScope.launch {
            mutableState.value = DetailUiState(isLoading = true)
            try {
                val document = getDocument(documentId)
                if (document != null) {
                    mutableState.value = DetailUiState(document = document, isLoading = false)
                } else {
                    mutableState.value = DetailUiState(
                        isLoading = false,
                        errorMessage = "Document not found"
                    )
                }
            } catch (e: Exception) {
                mutableState.value = DetailUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "An error occurred"
                )
            }
        }
    }
}