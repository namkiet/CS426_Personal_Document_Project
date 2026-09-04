package com.example.personaldocumentproject.presentation.modelmanager

import com.example.personaldocumentproject.data.ml.llm.ModelEntry

data class ModelRowState(
    val entry: ModelEntry,
    val isDownloaded: Boolean,
    val isActive: Boolean,
    val downloadPercent: Int? = null,   // null = not downloading, 0-100 = in progress
    val isDownloading: Boolean = false,
    val errorMessage: String? = null,
)

data class ModelManagerUiState(
    val models: List<ModelRowState> = emptyList(),
    val isLoading: Boolean = true,
)
