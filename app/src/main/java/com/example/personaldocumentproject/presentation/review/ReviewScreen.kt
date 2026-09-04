package com.example.personaldocumentproject.presentation.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
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
import com.example.personaldocumentproject.domain.model.DocumentDraft

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val BackgroundColor  = Color(0xFF111318)
private val SurfaceColor     = Color(0xFF1E2128)
private val SurfaceBorder    = Color(0xFF2E3340)
private val LabelColor       = Color(0xFF8B92A5)
private val ContentColor     = Color.White
private val AccentColor      = Color(0xFF6B8CFF)

// ── Route ─────────────────────────────────────────────────────────────────────

@Composable
fun ReviewRoute(
    imageUri: String,
    viewModel: ReviewViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(imageUri) { viewModel.analyze(imageUri) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onSaved() }
    ReviewScreen(
        state = state,
        onTitleChanged = viewModel::updateTitle,
        onSave = viewModel::save,
        onBack = onBack,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onTitleChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Review", color = ContentColor, fontWeight = FontWeight.SemiBold) },
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
            // ── Loading / error ───────────────────────────────────────────────
            if (state.isAnalyzing) {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = AccentColor)
                        Text("Analysing document…", color = LabelColor, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            state.errorMessage?.let {
                FieldBox(label = "Error") {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            state.draft?.let { draft ->
                DraftContent(
                    draft = draft,
                    isSaving = state.isSaving,
                    onTitleChanged = onTitleChanged,
                    onSave = onSave,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DraftContent(
    draft: DocumentDraft,
    isSaving: Boolean,
    onTitleChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    // ── Image preview ─────────────────────────────────────────────────────────
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = draft.imageUri,
            contentDescription = "Document image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }

    // ── Title (editable) ──────────────────────────────────────────────────────
    FieldBox(label = "Title") {
        OutlinedTextField(
            value = draft.title,
            onValueChange = onTitleChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ContentColor,
                unfocusedTextColor = ContentColor,
                focusedBorderColor = AccentColor,
                unfocusedBorderColor = SurfaceBorder,
                cursorColor = AccentColor,
            ),
        )
    }

    // ── Description ───────────────────────────────────────────────────────────
    FieldBox(label = "Description") {
        Text(
            text = draft.description.ifBlank { "—" },
            color = ContentColor,
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    // ── Category ─────────────────────────────────────────────────────────────
    FieldBox(label = "Category") {
        Text(
            text = draft.category.name,
            color = AccentColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }

    // ── Detected objects ──────────────────────────────────────────────────────
    if (draft.detectedObjects.isNotEmpty()) {
        FieldBox(label = "Detected Objects") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                draft.detectedObjects.forEach { obj ->
                    Chip(obj)
                }
            }
        }
    }

    // ── Tags ─────────────────────────────────────────────────────────────────
    if (draft.tags.isNotEmpty()) {
        FieldBox(label = "Tags") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                draft.tags.forEach { tag ->
                    Chip(tag)
                }
            }
        }
    }

    // ── OCR text ──────────────────────────────────────────────────────────────
    FieldBox(label = "Extracted Text") {
        Text(
            text = draft.ocrText.ifBlank { "No text detected." },
            color = ContentColor,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
        )
    }

    // ── Save button ───────────────────────────────────────────────────────────
    Button(
        onClick = onSave,
        enabled = !isSaving,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(if (isSaving) "Saving…" else "Save Document")
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
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing,
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
