package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.data.local.AppDatabase
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.data.repository.DefaultDocumentRepository
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.service.EmbeddingService
import com.example.personaldocumentproject.domain.service.ImageLabelingService
import com.example.personaldocumentproject.domain.service.ImageStorageService
import com.example.personaldocumentproject.domain.service.OcrService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentWorkflowTest {
    private val database = AppDatabase()
    private val repository = DefaultDocumentRepository(
        database.documentDao,
        DocumentMapper(),
    )
    private val dummyImageStorageService = object : ImageStorageService {
        override suspend fun saveImageLocally(sourceUri: String): String = sourceUri
    }

    @Test
    fun analyze_then_save_persists_document() = runBlocking {
        val analyze = AnalyzeDocument(
            ocrService = object : OcrService {
                override suspend fun recognizeText(imageUri: String) = "Database indexing\nB-tree"
            },
            embeddingService = object : EmbeddingService {
                override suspend fun embed(text: String) = listOf(1f, 0f)
            },
            imageLabelingService = object : ImageLabelingService {
                override suspend fun generateTags(imageUri: String) = listOf("lecture")
            },
        )
        val save = SaveDocument(repository, dummyImageStorageService, idGenerator = { "id" }, clock = { 42L })

        val document = save(analyze("content://lecture"))

        assertEquals("Database indexing", document.title)
        assertEquals(DocumentCategory.OTHER, document.category)
        assertEquals(document, repository.get("id"))
    }

    @Test
    fun documents_are_observed_newest_first() = runBlocking {
        val save = SaveDocument(repository, dummyImageStorageService, idGenerator = { "old" }, clock = { 1L })
        save(com.example.personaldocumentproject.domain.model.DocumentDraft("content://old"))
        SaveDocument(repository, dummyImageStorageService, idGenerator = { "new" }, clock = { 2L })(
            com.example.personaldocumentproject.domain.model.DocumentDraft("content://new"),
        )

        assertEquals(listOf("new", "old"), repository.observeAll().first().map { it.id })
    }
}
