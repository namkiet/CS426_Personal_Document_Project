package com.example.personaldocumentproject.presentation.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
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
        onEnhance = viewModel::enhanceData,
        onSave = viewModel::save,
        onBack = onBack
    )
}

@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onTitleChanged: (String) -> Unit,
    onEnhance: (String?) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    var languageHint by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Review document", color = Color.White)
            if (state.isAnalyzing) CircularProgressIndicator()
            state.draft?.let { draft ->
                DraftTitleField(draft.title, onTitleChanged)

                if (draft.tags.isNotEmpty()) {
                    Text("Image Labels (Visual Content)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(draft.tags.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }

                Text("OCR Extracted Text", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(draft.extractedText.ifBlank { "No text was detected." }, color = Color.White)

                if (state.isEnhancing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator()
                        Text("Enhancing with AI...", color = Color.White)
                    }
                } else {
                    OutlinedTextField(
                        value = languageHint,
                        onValueChange = { languageHint = it },
                        label = { Text("Language Hint (Optional)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    OutlinedButton(
                        onClick = { onEnhance(languageHint) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enhance Data with AI", color = Color.White)
                    }
                }

                Button(onClick = onSave, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.isSaving) "Saving…" else "Save document")
                }
            }
            state.errorMessage?.let { Text(it, color = Color.White) }
            OutlinedButton(onClick = onBack) { Text("Back", color = Color.White) }
        }
    }
}
