package com.example.personaldocumentproject.presentation.review

import com.example.personaldocumentproject.domain.model.DocumentDraft

data class ReviewUiState(
    val draft: DocumentDraft? = null,
    val isAnalyzing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    // ── AI enhancement ─────────────────────────────────────────────────────
    /** True while Gemma is streaming a response. */
    val isEnhancing: Boolean = false,
    /** Streamed / completed enhanced OCR text produced by the LLM. Null = not yet requested. */
    val enhancedOcrText: String? = null,
)
