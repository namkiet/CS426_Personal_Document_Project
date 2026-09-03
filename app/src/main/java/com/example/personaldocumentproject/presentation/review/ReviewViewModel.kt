package com.example.personaldocumentproject.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.service.LlmService
import com.example.personaldocumentproject.domain.usecase.AnalyzeDocument
import com.example.personaldocumentproject.domain.usecase.SaveDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val analyzeDocument: AnalyzeDocument,
    private val saveDocument: SaveDocument,
    /** LLM used for OCR enhancement. Swap to qwenLlmService in AppContainer to switch models. */
    private val llmService: LlmService,
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

    /**
     * Sends the raw OCR text to the LLM and streams back an organised, clean version.
     *
     * The prompt instructs the model to:
     * - Fix OCR errors and garbled characters
     * - Restore paragraph / sentence structure
     * - Return plain text only (no markdown fences)
     *
     * Each streamed token chunk is appended live so the user sees the output as it arrives.
     *
     * **To switch the model**: in [AppContainer.reviewViewModelFactory] change
     * `gemmaLlmService` → `qwenLlmService` (or any other [LlmService] implementation).
     */
    fun enhanceWithAi() {
        val rawText = mutableState.value.draft?.ocrText ?: return
        if (rawText.isBlank() || mutableState.value.isEnhancing) return

        val prompt = buildEnhancePrompt(rawText)

        viewModelScope.launch {
            // Reset previous result and mark as loading
            mutableState.value = mutableState.value.copy(
                isEnhancing = true,
                enhancedOcrText = "",
            )
            try {
                llmService.generate(prompt).collect { tokenChunk ->
                    // Append each streamed token to the displayed text
                    mutableState.value = mutableState.value.copy(
                        enhancedOcrText = (mutableState.value.enhancedOcrText ?: "") + tokenChunk,
                    )
                }
            } catch (e: Exception) {
                mutableState.value = mutableState.value.copy(
                    errorMessage = "AI enhancement failed: ${e.message}",
                )
            } finally {
                mutableState.value = mutableState.value.copy(isEnhancing = false)
            }
        }
    }

    private fun buildEnhancePrompt(rawOcrText: String): String = """
        You are a document cleanup assistant.
        The text below was extracted by an OCR scanner and may contain errors, broken words, garbled characters, or lost formatting.

        Your task:
        1. Fix any OCR recognition errors (e.g. "l" mistaken for "1", merged or split words).
        2. Restore natural sentence and paragraph structure.
        3. Remove duplicate lines or noise characters.
        4. Output ONLY the cleaned plain text. Do NOT include explanations, headings, or markdown fences.

        Raw OCR text:
        ---
        $rawOcrText
        ---

        Cleaned text:
    """.trimIndent()
}
