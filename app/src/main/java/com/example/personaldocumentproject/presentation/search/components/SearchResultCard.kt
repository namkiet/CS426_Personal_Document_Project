package com.example.personaldocumentproject.presentation.search.components

import androidx.compose.runtime.Composable
import com.example.personaldocumentproject.domain.model.SearchResult
import com.example.personaldocumentproject.presentation.library.components.DocumentCard

@Composable
fun SearchResultCard(result: SearchResult) = DocumentCard(result.document)
