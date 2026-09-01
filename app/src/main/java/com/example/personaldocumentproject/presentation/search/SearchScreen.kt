package com.example.personaldocumentproject.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.presentation.search.components.SearchResultCard

@Composable
fun SearchRoute(viewModel: SearchViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    SearchScreen(state, viewModel::onQueryChanged, viewModel::performSearch, onBack)
}

@Composable
fun SearchScreen(state: SearchUiState, onQueryChanged: (String) -> Unit, onSearch: () -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Search documents")
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.query, onValueChange = onQueryChanged,
                modifier = Modifier.weight(1f), label = { Text("Describe what you need") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSearch,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(state.results, key = { it.document.id }) { SearchResultCard(it) }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
