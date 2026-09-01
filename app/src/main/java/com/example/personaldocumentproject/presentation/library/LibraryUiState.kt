package com.example.personaldocumentproject.presentation.library

import com.example.personaldocumentproject.domain.model.Document

data class LibraryUiState(val documents: List<Document> = emptyList())
