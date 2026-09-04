package com.example.personaldocumentproject.presentation.search.components

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.domain.model.SearchResult
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.domain.model.SearchResult
import com.example.personaldocumentproject.presentation.components.DocumentCard
import com.example.personaldocumentproject.presentation.components.DocumentCardLayoutConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultCard(result: SearchResult, onClick: () -> Unit = {}) = DocumentCard(result.document, onClick, config = DocumentCardLayoutConfig(false, 0.dp, 0.dp))
