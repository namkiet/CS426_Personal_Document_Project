package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.EmbeddingService
import com.example.personaldocumentproject.domain.service.ImageLabelingService
import com.example.personaldocumentproject.domain.service.OcrService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class AnalyzeDocument(
    private val ocrService: OcrService,
    private val embeddingService: EmbeddingService,
    private val imageLabelingService: ImageLabelingService,
) {
    suspend operator fun invoke(imageUri: String): DocumentDraft = coroutineScope {
        require(imageUri.isNotBlank()) { "An image URI is required" }

        // Run OCR and Labeling concurrently
        val textDeferred = async { ocrService.recognizeText(imageUri) }
        val tagsDeferred = async { imageLabelingService.generateTags(imageUri) }

        val text = textDeferred.await()
        val tags = tagsDeferred.await()

        val allSearchableText = "$text ${tags.joinToString(" ")}"

        DocumentDraft(
            imageUri = imageUri,
            title = text.lineSequence().firstOrNull { it.isNotBlank() }?.take(80) ?: "New document",
            description = if (text.isBlank()) "No text detected" else "Text extracted from the image",
            extractedText = text,
            tags = tags,
            embedding = embeddingService.embed(allSearchableText),
        )
    }
}
