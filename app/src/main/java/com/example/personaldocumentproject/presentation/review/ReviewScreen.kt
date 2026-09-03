package com.example.personaldocumentproject.presentation.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.presentation.review.components.DraftTitleField

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
        onEnhanceWithAi = viewModel::enhanceWithAi,
        onSave = viewModel::save,
        onBack = onBack,
    )
}

@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onTitleChanged: (String) -> Unit,
    onEnhanceWithAi: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Review document", color = Color.White, style = MaterialTheme.typography.titleLarge)

            if (state.isAnalyzing) CircularProgressIndicator()

            state.draft?.let { draft ->
                DraftTitleField(draft.title, onTitleChanged)

                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Description", style = MaterialTheme.typography.titleMedium)
                        SelectionContainer {
                            Text(draft.description.ifBlank { "No description generated." })
                        }
                    }
                }

                if (draft.tags.isNotEmpty()) {
                    Text(
                        "Image Labels (Visual Content)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        draft.tags.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                    )
                }

                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Extracted text", style = MaterialTheme.typography.titleMedium)
                        SelectionContainer {
                            Text(draft.ocrText.ifBlank { "No text was detected." })
                        }
                    }
                }

                // ── Enhance with AI button ────────────────────────────────────────
                // TEMPORARY: wire to a different model by swapping llmService in
                // AppContainer.reviewViewModelFactory (gemmaLlmService → qwenLlmService).
                Button(
                    onClick = onEnhanceWithAi,
                    enabled = draft.ocrText.isNotBlank() && !state.isEnhancing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ),
                ) {
                    if (state.isEnhancing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onTertiary,
                        )
                        Text("Enhancing…", modifier = Modifier.padding(start = 8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp),
                        )
                        Text("Enhance with AI  (Gemma 4)", modifier = Modifier.padding(start = 4.dp))
                    }
                }

                // ── Enhanced OCR result ───────────────────────────────────────────
                AnimatedVisibility(
                    visible = state.enhancedOcrText != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                "AI Enhanced Text",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .padding(12.dp),
                        ) {
                            Text(
                                text = state.enhancedOcrText
                                    ?.ifBlank { if (state.isEnhancing) "…" else "No output produced." }
                                    ?: "",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                // ── Save ──────────────────────────────────────────────────────────
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (state.isSaving) "Saving…" else "Save document")
                }
            }

            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OutlinedButton(onClick = onBack) { Text("Back", color = Color.White) }
        }
    }
}
