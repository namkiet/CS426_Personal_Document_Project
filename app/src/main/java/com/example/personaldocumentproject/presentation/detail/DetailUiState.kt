package com.example.personaldocumentproject.presentation.detail

import com.example.personaldocumentproject.domain.model.Document

data class DetailUiState(
    val document: Document? = null,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)
