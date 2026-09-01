package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.repository.DocumentRepository
import java.util.UUID

class SaveDocument(
    private val repository: DocumentRepository,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend operator fun invoke(draft: DocumentDraft): Document {
        require(draft.imageUri.isNotBlank()) { "An image URI is required" }
        return Document(
            id = idGenerator(), imageUri = draft.imageUri,
            title = draft.title.ifBlank { "Untitled document" },
            description = draft.description, extractedText = draft.extractedText,
            category = draft.category, tags = draft.tags, embedding = draft.embedding,
            createdAtEpochMillis = clock(),
        ).also { repository.save(it) }
    }
}
