package com.example.personaldocumentproject.presentation.library

import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.SearchResult

data class LibraryUiState(
    val documents: List<Document> = emptyList(),
    val query: String = "",
    val errorMessage: String? = null,
    /** Non-null only while a search query is active; carries per-document scores. */
    val searchResults: Map<String, SearchResult> = emptyMap(),
    val selectedIds: Set<String> = emptySet(),
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}
