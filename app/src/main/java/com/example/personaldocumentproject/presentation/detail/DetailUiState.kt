package com.example.personaldocumentproject.presentation.detail

import com.example.personaldocumentproject.domain.model.Document

data class DetailUiState(
    val document: Document? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)