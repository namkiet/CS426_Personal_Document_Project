package com.example.personaldocumentproject.presentation.review

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
import androidx.compose.material3.Button
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
        onSave = viewModel::save,
        onBack = onBack,
    )
}

@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onTitleChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        if (isLandscape) {
            // Landscape: Side-by-side two-pane layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left pane: Image preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    val draft = state.draft
                    if (draft != null) {
                        AsyncImage(
                            model = draft.imageUri,
                            contentDescription = "Document Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else if (state.isAnalyzing) {
                        CircularProgressIndicator()
                    }
                }

                // Right pane: Form and actions
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Review document", color = Color.White, style = MaterialTheme.typography.titleLarge)

                    if (state.isAnalyzing) CircularProgressIndicator()

                    state.draft?.let { draft ->
                        DraftTitleField(draft.title, onTitleChanged)

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

                        Text(
                            "OCR Extracted Text",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                        Text(
                            draft.ocrText.ifBlank { "No text was detected." },
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                        )

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
        } else {
            // Portrait: Vertical scrollable column
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
                    AsyncImage(
                        model = draft.imageUri,
                        contentDescription = "Document Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Fit
                    )

                    DraftTitleField(draft.title, onTitleChanged)

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

                    Text(
                        "OCR Extracted Text",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        draft.ocrText.ifBlank { "No text was detected." },
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                    )

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
}
