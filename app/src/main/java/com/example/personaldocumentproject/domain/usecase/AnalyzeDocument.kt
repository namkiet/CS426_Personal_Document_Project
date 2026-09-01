package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.EmbeddingService
import com.example.personaldocumentproject.domain.service.OcrService

class AnalyzeDocument(
    private val ocrService: OcrService,
    private val embeddingService: EmbeddingService,
) {
    suspend operator fun invoke(imageUri: String): DocumentDraft {
        require(imageUri.isNotBlank()) { "An image URI is required" }
        val text = ocrService.recognizeText(imageUri)
        return DocumentDraft(
            imageUri = imageUri,
            title = text.lineSequence().firstOrNull { it.isNotBlank() }?.take(80) ?: "New document",
            description = if (text.isBlank()) "No text detected" else "Text extracted from the image",
            extractedText = text,
            embedding = embeddingService.embed(text),
        )
    }
}
