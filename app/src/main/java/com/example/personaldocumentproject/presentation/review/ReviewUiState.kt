package com.example.personaldocumentproject.presentation.review

import com.example.personaldocumentproject.domain.model.DocumentDraft

data class ReviewUiState(
    val draft: DocumentDraft? = null,
    val isAnalyzing: Boolean = false,
    val isEnhancing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
)
