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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.model.DocumentDraft

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val BackgroundColor = Color(0xFF111318)
private val SurfaceColor    = Color(0xFF1E2128)
private val SurfaceBorder   = Color(0xFF2E3340)
private val LabelColor      = Color(0xFF8B92A5)
private val ContentColor    = Color.White
private val AccentColor     = Color(0xFF6B8CFF)
private val ChipBackground  = Color(0xFF2A3050)

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
        state              = state,
        onTitleChanged     = viewModel::updateTitle,
        onDescriptionChanged = viewModel::updateDescription,
        onOcrTextChanged   = viewModel::updateOcrText,
        onCategoryChanged  = viewModel::updateCategory,
        onTagsChanged      = viewModel::updateTags,
        onObjectsChanged   = viewModel::updateDetectedObjects,
        onSave             = viewModel::save,
        onBack             = onBack,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onOcrTextChanged: (String) -> Unit,
    onCategoryChanged: (DocumentCategory) -> Unit,
    onTagsChanged: (List<String>) -> Unit,
    onObjectsChanged: (List<String>) -> Unit,
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
            // ── Loading ───────────────────────────────────────────────────────
            if (state.isAnalyzing) {
                Box(
                    Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
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
                    draft                = draft,
                    isSaving             = state.isSaving,
                    onTitleChanged       = onTitleChanged,
                    onDescriptionChanged = onDescriptionChanged,
                    onOcrTextChanged     = onOcrTextChanged,
                    onCategoryChanged    = onCategoryChanged,
                    onTagsChanged        = onTagsChanged,
                    onObjectsChanged     = onObjectsChanged,
                    onSave               = onSave,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Draft content ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DraftContent(
    draft: DocumentDraft,
    isSaving: Boolean,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onOcrTextChanged: (String) -> Unit,
    onCategoryChanged: (DocumentCategory) -> Unit,
    onTagsChanged: (List<String>) -> Unit,
    onObjectsChanged: (List<String>) -> Unit,
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

    // ── Title ─────────────────────────────────────────────────────────────────
    FieldBox(label = "Title") {
        EditableTextField(
            value = draft.title,
            onValueChange = onTitleChanged,
            singleLine = true,
        )
    }

    // ── Description ───────────────────────────────────────────────────────────
    FieldBox(label = "Description") {
        EditableTextField(
            value = draft.description,
            onValueChange = onDescriptionChanged,
            minLines = 2,
        )
    }

    // ── Category ──────────────────────────────────────────────────────────────
    FieldBox(label = "Category") {
        CategoryDropdown(selected = draft.category, onSelected = onCategoryChanged)
    }

    // ── Detected objects ──────────────────────────────────────────────────────
    FieldBox(label = "Detected Objects") {
        ChipEditor(
            items = draft.detectedObjects,
            onItemsChanged = onObjectsChanged,
            placeholder = "Add object…",
        )
    }

    // ── Tags ─────────────────────────────────────────────────────────────────
    FieldBox(label = "Tags") {
        ChipEditor(
            items = draft.tags,
            onItemsChanged = onTagsChanged,
            placeholder = "Add tag…",
        )
    }

    // ── OCR text ──────────────────────────────────────────────────────────────
    FieldBox(label = "Extracted Text") {
        EditableTextField(
            value = draft.ocrText,
            onValueChange = onOcrTextChanged,
            minLines = 4,
            placeholder = "No text detected.",
        )
    }

    // ── Save ──────────────────────────────────────────────────────────────────
    Button(
        onClick = onSave,
        enabled = !isSaving,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(if (isSaving) "Saving…" else "Save Document")
    }
}

// ── Reusable field components ─────────────────────────────────────────────────

@Composable
private fun EditableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = false,
    minLines: Int = 1,
    placeholder: String = "",
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        placeholder = {
            if (placeholder.isNotBlank())
                Text(placeholder, color = LabelColor, style = MaterialTheme.typography.bodySmall)
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor   = ContentColor,
            unfocusedTextColor = ContentColor,
            focusedBorderColor   = AccentColor,
            unfocusedBorderColor = SurfaceBorder,
            cursorColor          = AccentColor,
        ),
        textStyle = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun CategoryDropdown(
    selected: DocumentCategory,
    onSelected: (DocumentCategory) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selected.name, color = AccentColor, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = AccentColor, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = SurfaceColor,
        ) {
            DocumentCategory.entries.forEach { cat ->
                DropdownMenuItem(
                    text = {
                        Text(
                            cat.name,
                            color = if (cat == selected) AccentColor else ContentColor,
                            fontWeight = if (cat == selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSelected(cat)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipEditor(
    items: List<String>,
    onItemsChanged: (List<String>) -> Unit,
    placeholder: String,
) {
    var inputText by rememberSaveable { mutableStateOf("") }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items.forEach { item ->
            RemovableChip(
                text = item,
                onRemove = { onItemsChanged(items - item) },
            )
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 6.dp),
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = {
                Text(placeholder, color = LabelColor, style = MaterialTheme.typography.bodySmall)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor   = ContentColor,
                unfocusedTextColor = ContentColor,
                focusedBorderColor   = AccentColor,
                unfocusedBorderColor = SurfaceBorder,
                cursorColor          = AccentColor,
            ),
            textStyle = MaterialTheme.typography.bodySmall,
        )
        IconButton(
            onClick = {
                val trimmed = inputText.trim()
                if (trimmed.isNotBlank() && !items.contains(trimmed)) {
                    onItemsChanged(items + trimmed)
                }
                inputText = ""
            },
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add", tint = AccentColor)
        }
    }
}

@Composable
private fun RemovableChip(text: String, onRemove: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ChipBackground)
            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = AccentColor, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.width(2.dp))
        IconButton(onClick = onRemove, modifier = Modifier.size(18.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Remove $text", tint = LabelColor, modifier = Modifier.size(12.dp))
        }
    }
}

// ── FieldBox ──────────────────────────────────────────────────────────────────

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
