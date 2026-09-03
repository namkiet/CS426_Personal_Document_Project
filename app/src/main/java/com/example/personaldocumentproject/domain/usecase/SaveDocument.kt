package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.repository.DocumentRepository
import com.example.personaldocumentproject.domain.service.EmbeddingService
import com.example.personaldocumentproject.domain.service.ImageStorageService
import java.util.UUID

class SaveDocument(
    private val repository: DocumentRepository,
    private val imageStorageService: ImageStorageService,
    private val embeddingService: EmbeddingService,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend operator fun invoke(draft: DocumentDraft): Document {
        require(draft.imageUri.isNotBlank()) { "An image URI is required" }

        // 01. Save the selected image to the app's internal storage.
        val localImageUri = imageStorageService.saveImageLocally(draft.imageUri)

        // 02. Build the text representation and generate its embedding for semantic search.
        val embeddingText = """
            ${draft.description}
            ${draft.ocrText}
            ${draft.tags.joinToString(", ")}
            ${draft.detectedObjects.joinToString(", ")}
        """.trimIndent()
        val embedding = embeddingService.embed(embeddingText)

        // 03. Create the final document from the reviewed draft and generated data.
        val document = Document(
            id = idGenerator(),
            imageUri = localImageUri,
            title = draft.title.ifBlank { "Untitled document" },
            description = draft.description, 
            ocrText = draft.ocrText,
            category = draft.category, 
            tags = draft.tags, 
            detectedObjects = draft.detectedObjects,
            embedding = embedding,
            createdAt = clock(),
        )

        // 04. Persist the document metadata in the repository.
        repository.save(document)

        // 05. Return the saved document to the caller.
        return document
    }
}
