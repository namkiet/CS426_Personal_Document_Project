package com.example.personaldocumentproject.presentation.detail

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Surface
        }

        if (isLandscape) {
            // Landscape: Side-by-side two-pane layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left pane: Full-height image
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    state.document?.let { document ->
                        AsyncImage(
                            model = document.imageUri,
                            contentDescription = "Document Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Right pane: Scrollable metadata and action
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.errorMessage?.let { error ->
                        Text("Error: $error", color = MaterialTheme.colorScheme.error)
                    }

                    state.document?.let { document ->
                        Text(document.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)

                        Text("Description", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(document.description, style = MaterialTheme.typography.bodyMedium, color = Color.White)

                        if (document.tags.isNotEmpty()) {
                            Text("Image Labels (Visual Content)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(document.tags.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }

                        Text("Extracted Text", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(document.ocrText.ifBlank { "No text detected." }, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }

                    OutlinedButton(onClick = onBack) {
                        Text("Back", color = Color.White)
                    }
                }
            }
        } else {
            // Portrait: Vertical scrollable column
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

                    Text("Extracted Text", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(document.ocrText.ifBlank { "No text detected." }, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }

                OutlinedButton(onClick = onBack) {
                    Text("Back", color = Color.White)
                }
            }
        }
    }
}
