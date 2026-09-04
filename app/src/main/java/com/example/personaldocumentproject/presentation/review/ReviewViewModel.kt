package com.example.personaldocumentproject.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.model.DocumentDraft
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

    fun analyze(imageUri: String) {
        if (mutableState.value.draft?.imageUri == imageUri || mutableState.value.isAnalyzing) return
        viewModelScope.launch {
            mutableState.value = ReviewUiState(isAnalyzing = true)
            mutableState.value = runCatching { analyzeDocument(imageUri) }
                .fold(
                    onSuccess = { ReviewUiState(draft = it) },
                    onFailure = { ReviewUiState(errorMessage = it.message ?: "Analysis failed") },
                )
        }
    }

    fun updateTitle(title: String) = updateDraft { it.copy(title = title) }
    fun updateDescription(description: String) = updateDraft { it.copy(description = description) }
    fun updateCategory(category: DocumentCategory) = updateDraft { it.copy(category = category) }
    fun updateTags(tags: List<String>) = updateDraft { it.copy(tags = tags) }
    fun updateDetectedObjects(objects: List<String>) = updateDraft { it.copy(detectedObjects = objects) }

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

    private fun updateDraft(transform: (DocumentDraft) -> DocumentDraft) {
        val state = mutableState.value
        val draft = state.draft ?: return
        mutableState.value = state.copy(draft = transform(draft))
    }
}
