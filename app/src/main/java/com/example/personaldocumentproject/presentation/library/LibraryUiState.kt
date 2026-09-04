package com.example.personaldocumentproject.presentation.library

import com.example.personaldocumentproject.domain.model.Document

data class LibraryUiState(
    val documents: List<Document> = emptyList(),
    val allDocuments: List<Document> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isSearchActive: Boolean = false,
    val tagQuery: String = "",
    val availableTags: List<String> = emptyList(),
    val filteredTags: List<String> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val isTagDropdownExpanded: Boolean = false,
    val errorMessage: String? = null,
)
