package com.example.personaldocumentproject.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
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
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.document?.title ?: "Document",
                        color = ContentColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ContentColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Loading ───────────────────────────────────────────────────────
            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentColor)
                }
            }

            // ── Error ─────────────────────────────────────────────────────────
            state.errorMessage?.let {
                FieldBox(label = "Error") {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // ── Document fields ───────────────────────────────────────────────
            state.document?.let { doc ->
                DocumentContent(doc)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DocumentContent(doc: Document) {
    // ── Image ─────────────────────────────────────────────────────────────────
    Box(
        Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = doc.imageUri,
            contentDescription = "Document image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }

    // ── Title ─────────────────────────────────────────────────────────────────
    FieldBox(label = "Title") {
        Text(doc.title, color = ContentColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
    }

    // ── Description ───────────────────────────────────────────────────────────
    FieldBox(label = "Description") {
        Text(
            text = doc.description.ifBlank { "—" },
            color = ContentColor,
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    // ── Category ─────────────────────────────────────────────────────────────
    FieldBox(label = "Category") {
        Text(
            text = doc.category.name,
            color = AccentColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }

    // ── Detected objects ──────────────────────────────────────────────────────
    if (doc.detectedObjects.isNotEmpty()) {
        FieldBox(label = "Detected Objects") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                doc.detectedObjects.forEach { Chip(it) }
            }
        }
    }

    // ── Tags ─────────────────────────────────────────────────────────────────
    if (doc.tags.isNotEmpty()) {
        FieldBox(label = "Tags") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                doc.tags.forEach { Chip(it) }
            }
        }
    }

    // ── OCR text ──────────────────────────────────────────────────────────────
    FieldBox(label = "Extracted Text") {
        Text(
            text = doc.ocrText.ifBlank { "No text detected." },
            color = ContentColor,
            style = MaterialTheme.typography.bodySmall,
        )
    }

    // ── Saved at ─────────────────────────────────────────────────────────────
    FieldBox(label = "Saved") {
        val formatted = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(doc.createdAt))
        Text(formatted, color = LabelColor, style = MaterialTheme.typography.bodySmall)
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun FieldBox(label: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = LabelColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2A3050))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, color = AccentColor, style = MaterialTheme.typography.labelMedium)
    }
}
