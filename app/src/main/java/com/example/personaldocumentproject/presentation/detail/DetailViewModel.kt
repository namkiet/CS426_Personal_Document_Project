package com.example.personaldocumentproject.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.usecase.GetDocument
import com.example.personaldocumentproject.domain.usecase.DeleteDocument
import com.example.personaldocumentproject.domain.model.MockDocuments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val getDocument: GetDocument,
    private val deleteDocument: DeleteDocument,
) : ViewModel() {
    private val mutableState = MutableStateFlow(DetailUiState())
    val uiState = mutableState.asStateFlow()

    fun loadDocument(documentId: String) {
        if (mutableState.value.document?.id == documentId) return

        viewModelScope.launch {
            mutableState.value = DetailUiState(isLoading = true)
            try {
                val document = getDocument(documentId) ?: MockDocuments.find(documentId)
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

    fun delete() {
        val document = mutableState.value.document ?: return
        if (mutableState.value.isDeleting) return
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isDeleting = true, errorMessage = null)
            runCatching { deleteDocument(document.id) }
                .onSuccess {
                    mutableState.value = mutableState.value.copy(isDeleting = false, isDeleted = true)
                }
                .onFailure { error ->
                    mutableState.value = mutableState.value.copy(
                        isDeleting = false,
                        errorMessage = error.message ?: "Could not delete document",
                    )
                }
        }
    }
}
