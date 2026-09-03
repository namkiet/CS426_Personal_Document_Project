package com.example.personaldocumentproject.presentation.search.components

import androidx.compose.runtime.Composable
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.SearchResult

fun searchResultConverter(
    searchResult: List<SearchResult>
) : List<Document> {
    return searchResult.map { it.document }
}