package com.example.personaldocumentproject.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.usecase.AnalyzeDocument
import com.example.personaldocumentproject.domain.usecase.SaveDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val analyzeDocument: AnalyzeDocument,
    private val saveDocument: SaveDocument,
) : ViewModel() {

    private val mutableState = MutableStateFlow(ReviewUiState())
    val uiState = mutableState.asStateFlow()

    // ── Analysis ──────────────────────────────────────────────────────────────

    fun analyze(imageUri: String) {
        if (mutableState.value.draft?.imageUri == imageUri || mutableState.value.isAnalyzing) return
        viewModelScope.launch {
            mutableState.value = ReviewUiState(isAnalyzing = true)
            mutableState.value = runCatching { analyzeDocument(imageUri) }
                .fold(
                    onSuccess  = { ReviewUiState(draft = it) },
                    onFailure  = { ReviewUiState(errorMessage = it.message ?: "Analysis failed") },
                )
        }
    }

    // ── Field editors ─────────────────────────────────────────────────────────

    fun updateTitle(title: String) = editDraft { it.copy(title = title) }

    fun updateDescription(description: String) = editDraft { it.copy(description = description) }

    fun updateOcrText(ocrText: String) = editDraft { it.copy(ocrText = ocrText) }

    fun updateCategory(category: DocumentCategory) = editDraft { it.copy(category = category) }

    /** Replace the full tags list (caller builds the list from chip-add/remove). */
    fun updateTags(tags: List<String>) = editDraft { it.copy(tags = tags) }

    /** Replace the full detectedObjects list. */
    fun updateDetectedObjects(objects: List<String>) = editDraft { it.copy(detectedObjects = objects) }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun save() {
        val draft = mutableState.value.draft ?: return
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isSaving = true)
            mutableState.value = runCatching { saveDocument(draft) }
                .fold(
                    onSuccess = { mutableState.value.copy(isSaving = false, isSaved = true) },
                    onFailure = { mutableState.value.copy(isSaving = false, errorMessage = it.message) },
                )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun editDraft(block: (com.example.personaldocumentproject.domain.model.DocumentDraft)
        -> com.example.personaldocumentproject.domain.model.DocumentDraft) {
        val current = mutableState.value
        val draft = current.draft ?: return
        mutableState.value = current.copy(draft = block(draft))
    }
}
