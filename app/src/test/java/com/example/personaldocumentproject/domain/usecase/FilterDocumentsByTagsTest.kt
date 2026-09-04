package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterDocumentsByTagsTest {

    private val filterDocuments = FilterDocumentsByTags()

    @Test
    fun invoke_emptySelectedTags_returnsAllDocuments() {
        val docs = listOf(
            createDocument(id = "1", tags = listOf("receipt")),
            createDocument(id = "2", tags = listOf("invoice"))
        )

        val result = filterDocuments(docs, emptySet())

        assertEquals(docs, result)
    }

    @Test
    fun invoke_singleTag_matchesCaseInsensitive() {
        val doc1 = createDocument(id = "1", tags = listOf("Receipt", "Store"))
        val doc2 = createDocument(id = "2", tags = listOf("invoice"))
        val doc3 = createDocument(id = "3", tags = listOf("receipt"))

        val result = filterDocuments(listOf(doc1, doc2, doc3), setOf("RECEIPT"))

        assertEquals(listOf(doc1, doc3), result)
    }

    @Test
    fun invoke_multipleTags_requiresAllSelectedTags() {
        val doc1 = createDocument(id = "1", tags = listOf("tax", "2024", "receipt"))
        val doc2 = createDocument(id = "2", tags = listOf("tax", "2023"))
        val doc3 = createDocument(id = "3", tags = listOf("tax", "2024"))

        val result = filterDocuments(listOf(doc1, doc2, doc3), setOf("tax", "2024"))

        assertEquals(listOf(doc1, doc3), result)
    }

    private fun createDocument(id: String, tags: List<String>): Document {
        return Document(
            id = id,
            imageUri = "content://$id",
            title = "Doc $id",
            description = "Desc",
            ocrText = "OCR",
            category = DocumentCategory.OTHER,
            tags = tags,
            detectedObjects = emptyList(),
            embedding = emptyList(),
            createdAt = 1000L
        )
    }
}
