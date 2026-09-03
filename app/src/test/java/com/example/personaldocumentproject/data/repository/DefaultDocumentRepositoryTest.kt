package com.example.personaldocumentproject.data.repository

import com.example.personaldocumentproject.data.local.DocumentDao
import com.example.personaldocumentproject.data.local.DocumentEntity
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDocumentRepositoryTest {
    @Test
    fun search_ranks_matching_document() = runBlocking {
        val repository = DefaultDocumentRepository(
            FakeDocumentDao(),
            DocumentMapper(),
        )
        repository.save(document("database", "Database lecture", listOf(1f, 0f)))
        repository.save(document("cooking", "Cooking notes", listOf(0f, 1f)))

        val result = repository.search("database", listOf(1f, 0f)).first()

        assertEquals("database", result.first().document.id)
    }

    @Test
    fun search_does_not_match_substrings() = runBlocking {
        val repository = DefaultDocumentRepository(FakeDocumentDao(), DocumentMapper())
        repository.save(document("education", "Education notes", listOf(0f, 1f)))

        val result = repository.search("cat", listOf(1f, 0f)).first()

        assertEquals(emptyList<Document>(), result.map { it.document })
    }

    private fun document(id: String, title: String, embedding: List<Float>) = Document(
        id = id, imageUri = "content://$id", title = title, description = "",
        ocrText = "", category = DocumentCategory.OTHER, tags = emptyList(),
        detectedObjects = emptyList(),
        embedding = embedding, createdAt = 1,
    )
}

private class FakeDocumentDao : DocumentDao {
    private val documents = MutableStateFlow<List<DocumentEntity>>(emptyList())

    override fun observeAll() = documents

    override suspend fun get(id: String) = documents.value.firstOrNull { it.id == id }

    override suspend fun upsert(document: DocumentEntity) {
        documents.update { current ->
            (current.filterNot { it.id == document.id } + document)
                .sortedByDescending(DocumentEntity::createdAt)
        }
    }
}
