package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.data.local.DocumentDao
import com.example.personaldocumentproject.data.local.DocumentEntity
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.data.ml.embedding.SimpleEmbeddingService
import com.example.personaldocumentproject.data.ml.metadata.SimpleMetadataService
import com.example.personaldocumentproject.data.repository.DefaultDocumentRepository
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.ImageStorageService
import com.example.personaldocumentproject.domain.service.ObjectDetectionService
import com.example.personaldocumentproject.domain.service.OcrService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DocumentWorkflowTest {
    private val repository = DefaultDocumentRepository(FakeDao(), DocumentMapper())
    private val dummyImageStorage = object : ImageStorageService {
        override suspend fun saveImageLocally(sourceUri: String): String = sourceUri
    }

    @Test
    fun analyze_then_save_persists_document() = runBlocking {
        val analyze = AnalyzeDocument(
            ocrService = object : OcrService {
                override suspend fun recognizeText(imageUri: String) = "Database indexing\nB-tree"
            },
            objectDetectionService = object : ObjectDetectionService {
                override suspend fun detectObjects(imageUri: String) = listOf("book")
            },
            metadataService = SimpleMetadataService(),
        )
        val save = SaveDocument(
            repository = repository,
            imageStorageService = dummyImageStorage,
            embeddingService = SimpleEmbeddingService(),
            idGenerator = { "id" },
            clock = { 42L },
        )

        val document = save(analyze("content://lecture"))

        assertEquals("Database indexing", document.title)
        assertEquals(DocumentCategory.OTHER, document.category)
        assertNotNull(repository.get("id"))
        assertEquals(document, repository.get("id"))
    }

    @Test
    fun documents_are_observed_newest_first() = runBlocking {
        SaveDocument(
            repository = repository,
            imageStorageService = dummyImageStorage,
            embeddingService = SimpleEmbeddingService(),
            idGenerator = { "old" },
            clock = { 1L },
        )(
            DocumentDraft(
                imageUri = "content://old",
                title = "Old doc",
                description = "",
                ocrText = "",
                category = DocumentCategory.OTHER,
                tags = emptyList(),
                detectedObjects = emptyList(),
            )
        )

        SaveDocument(
            repository = repository,
            imageStorageService = dummyImageStorage,
            embeddingService = SimpleEmbeddingService(),
            idGenerator = { "new" },
            clock = { 2L },
        )(
            DocumentDraft(
                imageUri = "content://new",
                title = "New doc",
                description = "",
                ocrText = "",
                category = DocumentCategory.OTHER,
                tags = emptyList(),
                detectedObjects = emptyList(),
            )
        )

        assertEquals(listOf("new", "old"), repository.observeAll().first().map { it.id })
    }
}

private class FakeDao : DocumentDao {
    private val store = MutableStateFlow<List<DocumentEntity>>(emptyList())

    override fun observeAll() = store

    override suspend fun get(id: String) = store.value.firstOrNull { it.id == id }

    override suspend fun upsert(document: DocumentEntity) {
        store.update { current ->
            (current.filterNot { it.id == document.id } + document)
                .sortedByDescending(DocumentEntity::createdAt)
        }
    }
}
