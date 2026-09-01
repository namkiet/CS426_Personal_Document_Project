package com.example.personaldocumentproject.presentation.search

import com.example.personaldocumentproject.domain.model.SearchResult

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
)
