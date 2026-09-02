package com.example.personaldocumentproject.data.repository

import com.example.personaldocumentproject.data.local.AppDatabase
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDocumentRepositoryTest {
    @Test
    fun search_ranks_matching_document() = runBlocking {
        val repository = DefaultDocumentRepository(
            AppDatabase().documentDao,
            DocumentMapper(),
        )
        repository.save(document("database", "Database lecture", listOf(1f, 0f)))
        repository.save(document("cooking", "Cooking notes", listOf(0f, 1f)))

        val result = repository.search("database", listOf(1f, 0f)).first()

        assertEquals("database", result.first().document.id)
    }

    private fun document(id: String, title: String, embedding: List<Float>) = Document(
        id = id, imageUri = "content://$id", title = title, description = "",
        ocrText = "", category = DocumentCategory.OTHER, tags = emptyList(),
        detectedObjects = emptyList(),
        embedding = embedding, createdAt = 1,
    )
}
