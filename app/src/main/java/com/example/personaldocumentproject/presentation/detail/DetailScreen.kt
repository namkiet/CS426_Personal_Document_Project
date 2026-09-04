package com.example.personaldocumentproject.presentation.detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.personaldocumentproject.domain.model.Document
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Colour tokens (mirrors ReviewScreen) ─────────────────────────────────────
private val BackgroundColor = Color(0xFF111318)
private val SurfaceColor    = Color(0xFF1E2128)
private val SurfaceBorder   = Color(0xFF2E3340)
private val LabelColor      = Color(0xFF8B92A5)
private val ContentColor    = Color.White
private val AccentColor     = Color(0xFF6B8CFF)

// ── Route ─────────────────────────────────────────────────────────────────────

@Composable
fun DetailRoute(
    documentId: String,
    viewModel: DetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(documentId) { viewModel.loadDocument(documentId) }
    DetailScreen(state = state, onBack = onBack)
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
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

                Text("Extracted Text", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(document.ocrText.ifBlank { "No text detected." }, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            }

            OutlinedButton(onClick = onBack) {
                Text("Back", color = Color.White)
            }
        }
    }
}
