package com.example.personaldocumentproject.presentation.search

import com.example.personaldocumentproject.domain.model.SearchResult
import com.example.personaldocumentproject.domain.model.DocumentCategory

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val dateFilter: DateFilter = DateFilter.ALL,
    val categoryFilter: CategoryFilter = CategoryFilter.ALL,
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
)

enum class DateFilter(val label: String, val ageMillis: Long?) {
    ALL("All time", null),
    WEEK("Past 7 days", 7 * 24 * 60 * 60 * 1_000L),
    MONTH("Past 30 days", 30 * 24 * 60 * 60 * 1_000L),
}

enum class CategoryFilter(val label: String, val category: DocumentCategory?) {
    ALL("All categories", null),
    NOTE("Note", DocumentCategory.NOTE),
    RECEIPT("Receipt", DocumentCategory.RECEIPT),
    IDENTITY("Identity", DocumentCategory.IDENTITY),
    STUDY("Study", DocumentCategory.STUDY),
    WORK("Work", DocumentCategory.WORK),
    OTHER("Other", DocumentCategory.OTHER),
}
