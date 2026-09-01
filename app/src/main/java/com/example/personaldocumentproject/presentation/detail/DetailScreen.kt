package com.example.personaldocumentproject.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun DetailRoute(
    documentId: String,
    viewModel: DetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }
    DetailScreen(state = state, onBack = onBack)
}

@Composable
fun DetailScreen(
    state: DetailUiState,
    onBack: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            }

            state.errorMessage?.let { error ->
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            state.document?.let { document ->
                AsyncImage(
                    model = document.imageUri,
                    contentDescription = "Document Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )

                Text(document.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)

                Text("Description", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(document.description, style = MaterialTheme.typography.bodyMedium, color = Color.White)

                if (document.tags.isNotEmpty()) {
                    Text("Image Labels (Visual Content)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(document.tags.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }

                Text("OCR Extracted Text", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(document.extractedText.ifBlank { "No text detected." }, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            }

            OutlinedButton(onClick = onBack) {
                Text("Back", color = Color.White)
            }
        }
    }
}