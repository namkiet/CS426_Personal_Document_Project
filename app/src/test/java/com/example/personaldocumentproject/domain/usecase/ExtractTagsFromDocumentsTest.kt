package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtractTagsFromDocumentsTest {

    private val extractTags = ExtractTagsFromDocuments()

    @Test
    fun invoke_extractsDeduplicatedAndSortedTags() {
        val docs = listOf(
            createDocument(id = "1", tags = listOf("work", "receipt", " tax ")),
            createDocument(id = "2", tags = listOf("TAX", "work", "invoice")),
            createDocument(id = "3", tags = listOf("", "   ", "travel"))
        )

        val tags = extractTags(docs)

        assertEquals(listOf("TAX", "invoice", "receipt", "tax", "travel", "work"), tags)
    }

    @Test
    fun invoke_withAdditionalTags_includesThemSorted() {
        val docs = listOf(
            createDocument(id = "1", tags = listOf("receipt"))
        )

        val tags = extractTags(docs, additionalTags = listOf("future-tag", "bills"))

        assertEquals(listOf("bills", "future-tag", "receipt"), tags)
    }

    @Test
    fun invoke_emptyDocuments_returnsEmptyOrOnlyAdditionalTags() {
        assertEquals(emptyList<String>(), extractTags(emptyList()))
        assertEquals(listOf("master-tag"), extractTags(emptyList(), listOf("master-tag")))
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
