package com.example.personaldocumentproject.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.presentation.library.components.DocumentCard

@Composable
fun LibraryRoute(viewModel: LibraryViewModel, onCapture: () -> Unit, onSearch: () -> Unit, onDocumentClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    LibraryScreen(state, onCapture, onSearch, onDocumentClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(state: LibraryUiState, onCapture: () -> Unit, onSearch: () -> Unit, onDocumentClick: (String) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Personal Document") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = onCapture, modifier = Modifier.fillMaxWidth()) { Text("Capture document") }
            OutlinedButton(onClick = onSearch, modifier = Modifier.fillMaxWidth()) { Text("Search library") }
            if (state.documents.isEmpty()) Text("Your document library is empty.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.documents, key = Document::id) { document ->
                    DocumentCard(
                        document = document,
                        onClick = { onDocumentClick(document.id) }
                    )
                }
            }
        }
    }
}
