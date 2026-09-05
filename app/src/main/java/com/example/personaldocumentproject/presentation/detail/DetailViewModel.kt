package com.example.personaldocumentproject.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.usecase.GetDocument
import com.example.personaldocumentproject.domain.usecase.DeleteDocument
import com.example.personaldocumentproject.domain.usecase.UpdateDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val getDocument: GetDocument,
    private val deleteDocument: DeleteDocument,
    private val updateDocument: UpdateDocument,
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

    fun startEditing() {
        val doc = mutableState.value.document ?: return
        mutableState.value = mutableState.value.copy(
            isEditing = true,
            editTitle = doc.title,
            editDescription = doc.description,
            editTags = doc.tags.joinToString(", "),
        )
    }

    fun cancelEditing() {
        mutableState.value = mutableState.value.copy(
            isEditing = false,
            errorMessage = null,
        )
    }

    fun onTitleChange(value: String) {
        mutableState.value = mutableState.value.copy(editTitle = value)
    }

    fun onDescriptionChange(value: String) {
        mutableState.value = mutableState.value.copy(editDescription = value)
    }

    fun onTagsChange(value: String) {
        mutableState.value = mutableState.value.copy(editTags = value)
    }

    fun saveEdit() {
        val doc = mutableState.value.document ?: return
        val state = mutableState.value
        if (state.isSaving) return

        viewModelScope.launch {
            mutableState.value = state.copy(isSaving = true, errorMessage = null)
            runCatching {
                val tags = state.editTags
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                updateDocument(
                    doc.copy(
                        title = state.editTitle.ifBlank { "Untitled document" },
                        description = state.editDescription,
                        tags = tags,
                    )
                )
            }.onSuccess { updated ->
                mutableState.value = mutableState.value.copy(
                    document = updated,
                    isEditing = false,
                    isSaving = false,
                )
            }.onFailure { error ->
                mutableState.value = mutableState.value.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "Could not save changes",
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
