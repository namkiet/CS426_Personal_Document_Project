package com.example.personaldocumentproject.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.presentation.search.components.SearchBar
import com.example.personaldocumentproject.presentation.search.components.SearchResultCard

@Composable
fun SearchRoute(viewModel: SearchViewModel, onBack: () -> Unit, onDocumentClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    SearchScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onSearch = viewModel::performSearch,
        onClearQuery = viewModel::onClearQuery,
        onBack = onBack,
        onDocumentClick = onDocumentClick
    )
}

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    onClearQuery: () -> Unit,
    onDocumentClick: (String) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchBar(state = state, onQueryChanged = onQueryChanged, onSearch = onSearch, onClearQuery = onClearQuery)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(state.results, key = { it.document.id }) { result ->
                    SearchResultCard(
                        result = result,
                        onClick = { onDocumentClick(result.document.id) }
                    )
                }
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back", color = Color.White) }
        }
    }
}
