package com.example.personaldocumentproject.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.usecase.AnalyzeDocument
import com.example.personaldocumentproject.domain.usecase.EnhanceDocumentData
import com.example.personaldocumentproject.domain.usecase.SaveDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val analyzeDocument: AnalyzeDocument,
    private val enhanceDocumentData: EnhanceDocumentData,
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

    fun updateTitle(title: String) {
        val draft = mutableState.value.draft ?: return
        mutableState.value = mutableState.value.copy(draft = draft.copy(title = title))
    }

    fun enhanceData(languageHint: String?) {
        val draft = mutableState.value.draft ?: return
        if (mutableState.value.isEnhancing || draft.extractedText.isBlank()) return

        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isEnhancing = true, errorMessage = null)
            mutableState.value = runCatching { enhanceDocumentData(draft, languageHint) }
                .fold(
                    onSuccess = { mutableState.value.copy(isEnhancing = false, draft = it) },
                    onFailure = { mutableState.value.copy(isEnhancing = false, errorMessage = "Failed to enhance data: ${it.message}") },
                )
        }
    }

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
}
