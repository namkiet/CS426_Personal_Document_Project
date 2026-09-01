package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.EmbeddingService
import com.example.personaldocumentproject.domain.service.LlmEnhancementService

class EnhanceDocumentData(
    private val llmService: LlmEnhancementService,
    private val embeddingService: EmbeddingService
) {
    suspend operator fun invoke(draft: DocumentDraft, languageHint: String? = null): DocumentDraft {
        if (draft.extractedText.isBlank()) return draft

        val enhancedText = llmService.enhanceDocumentData(
            ocrText = draft.extractedText,
            imageLabels = draft.tags,
            languageHint = languageHint
        )

        // Generate a new embedding for the enhanced text
        val newSearchableText = "$enhancedText ${draft.tags.joinToString(" ")}"
        val newEmbedding = embeddingService.embed(newSearchableText)

        return draft.copy(
            extractedText = enhancedText,
            embedding = newEmbedding
        )
    }
}