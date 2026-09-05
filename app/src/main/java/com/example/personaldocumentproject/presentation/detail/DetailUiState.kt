package com.example.personaldocumentproject.presentation.detail

import com.example.personaldocumentproject.domain.model.Document

data class DetailUiState(
    val document: Document? = null,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val editTitle: String = "",
    val editDescription: String = "",
    val editTags: String = "",
    val errorMessage: String? = null
)
