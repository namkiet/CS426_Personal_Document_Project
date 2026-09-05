package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.repository.DocumentRepository
import com.example.personaldocumentproject.domain.service.EmbeddingService

class UpdateDocument(
    private val repository: DocumentRepository,
    private val embeddingService: EmbeddingService,
) {
    suspend operator fun invoke(document: Document): Document {
        val embeddingText = """
            ${document.description}
            ${document.tags.joinToString(", ")}
            ${document.detectedObjects.joinToString(", ")}
            ${document.ocrText}
        """.trimIndent()
        val embedding = embeddingService.embed(embeddingText)

        val updated = document.copy(embedding = embedding)
        repository.save(updated)
        return updated
    }
}
