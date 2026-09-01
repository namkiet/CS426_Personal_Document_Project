package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.data.local.AppDatabase
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.data.repository.DefaultDocumentRepository
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.EmbeddingService
import com.example.personaldocumentproject.domain.service.ImageStorageService
import com.example.personaldocumentproject.domain.service.OcrService
import com.example.personaldocumentproject.domain.service.ObjectDetectionService
import com.example.personaldocumentproject.domain.service.TitleGenerationService
import com.example.personaldocumentproject.domain.service.DescriptionGenerationService
import com.example.personaldocumentproject.domain.service.CategoryClassificationService
import com.example.personaldocumentproject.domain.service.TagGenerationService
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

    @Test
    fun analyze_then_save_persists_document() = runBlocking {
        val analyze = AnalyzeDocument(
            ocrService = object : OcrService {
                override suspend fun recognizeText(imageUri: String) = "Database indexing\nB-tree"
            },
            objectDetectionService = object : ObjectDetectionService {
                override suspend fun detectObjects(imageUri: String) = listOf("book")
            },
            titleGenerationService = object : TitleGenerationService {
                override suspend fun generateTitle(ocrText: String, detectedObjects: List<String>) = "Database indexing"
            },
            descriptionGenerationService = object : DescriptionGenerationService {
                override suspend fun generateDescription(ocrText: String, detectedObjects: List<String>) = "Lecture notes"
            },
            categoryClassificationService = object : CategoryClassificationService {
                override suspend fun classifyCategory(ocrText: String, detectedObjects: List<String>) =
                    DocumentCategory.STUDY
            },
            tagGenerationService = object : TagGenerationService {
                override suspend fun generateTags(ocrText: String, detectedObjects: List<String>) = listOf("book")
            },
        )
        val save = SaveDocument(
            repository, imageStorageService, embeddingService,
            idGenerator = { "id" }, clock = { 42L },
        )

        val document = save(analyze("content://lecture"))

        assertEquals("Database indexing", document.title)
        assertEquals(DocumentCategory.STUDY, document.category)
        assertEquals(listOf("book"), document.tags)
        assertEquals(listOf("book"), document.detectedObjects)
        assertEquals(document, repository.get("id"))
    }

    @Test
    fun documents_are_observed_newest_first() = runBlocking {
        val save = SaveDocument(
            repository, imageStorageService, embeddingService,
            idGenerator = { "old" }, clock = { 1L },
        )
        save(draft("content://old"))
        SaveDocument(
            repository, imageStorageService, embeddingService,
            idGenerator = { "new" }, clock = { 2L },
        )(
            draft("content://new"),
        )

        assertEquals(listOf("new", "old"), repository.observeAll().first().map { it.id })
    }

    private val imageStorageService = object : ImageStorageService {
        override suspend fun saveImageLocally(sourceUri: String) = sourceUri
    }

    private val embeddingService = object : EmbeddingService {
        override suspend fun embed(text: String) = listOf(1f, 0f)
    }

    private fun draft(imageUri: String) = DocumentDraft(
        imageUri = imageUri,
        title = "",
        description = "",
        ocrText = "",
        category = DocumentCategory.OTHER,
        tags = emptyList(),
        detectedObjects = emptyList(),
    )
}
