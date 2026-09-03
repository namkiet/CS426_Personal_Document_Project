package com.example.personaldocumentproject.presentation.search.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.domain.model.SearchResult
import com.example.personaldocumentproject.presentation.components.DocumentCard
import com.example.personaldocumentproject.presentation.components.DocumentCardLayoutConfig

@Composable
fun SearchResultCard(result: SearchResult, onClick: () -> Unit = {}) = DocumentCard(result.document, onClick, config = DocumentCardLayoutConfig(false, 0.dp, 0.dp))
