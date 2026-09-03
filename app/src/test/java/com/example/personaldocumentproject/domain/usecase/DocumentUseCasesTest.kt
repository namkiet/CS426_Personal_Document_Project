package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.data.local.DocumentDao
import com.example.personaldocumentproject.data.local.DocumentEntity
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.data.ml.embedding.SimpleEmbeddingService
import com.example.personaldocumentproject.data.ml.metadata.SimpleDescriptionGenerationService
import com.example.personaldocumentproject.data.ml.metadata.SimpleTagGenerationService
import com.example.personaldocumentproject.data.ml.metadata.SimpleTitleGenerationService
import com.example.personaldocumentproject.data.repository.DefaultDocumentRepository
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.CategoryClassificationService
import com.example.personaldocumentproject.domain.service.DescriptionGenerationService
import com.example.personaldocumentproject.domain.service.ImageStorageService
import com.example.personaldocumentproject.domain.service.ObjectDetectionService
import com.example.personaldocumentproject.domain.service.OcrService
import com.example.personaldocumentproject.domain.service.TagGenerationService
import com.example.personaldocumentproject.domain.service.TextTranslationService
import com.example.personaldocumentproject.domain.service.TitleGenerationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentUseCasesTest {

    @Test
    fun analyzeDocument_generatesEveryDraftField() = runBlocking {
        val analyzeDocument = AnalyzeDocument(
            ocrService = FakeOcrService("University lecture\nDatabase indexing"),
            objectDetectionService = FakeObjectDetectionService(listOf("book", "laptop")),
            titleGenerationService = SimpleTitleGenerationService,
            descriptionGenerationService = SimpleDescriptionGenerationService,
            categoryClassificationService = object : CategoryClassificationService {
                override suspend fun classifyCategory(ocrText: String, detectedObjects: List<String>) =
                    DocumentCategory.STUDY
            },
            tagGenerationService = SimpleTagGenerationService,
            textTranslationService = FakeTextTranslationService(),
        )

        val draft = analyzeDocument("content://lecture")

        assertEquals("content://lecture", draft.imageUri)
        assertEquals("University lecture", draft.title)
        assertEquals("Text extracted from the image", draft.description)
        assertEquals("University lecture\nDatabase indexing", draft.ocrText)
        assertEquals(DocumentCategory.STUDY, draft.category)
        assertEquals(listOf("book", "laptop"), draft.tags)
        assertEquals(listOf("book", "laptop"), draft.detectedObjects)
    }

    @Test
    fun analyzeDocument_usesEnglishTranslationOnlyForDescription() = runBlocking {
        val titleGenerationService = object : TitleGenerationService {
            override suspend fun generateTitle(ocrText: String, detectedObjects: List<String>) = ocrText
        }
        val descriptionGenerationService = object : DescriptionGenerationService {
            override suspend fun generateDescription(ocrText: String, detectedObjects: List<String>) = ocrText
        }
        val categoryClassificationService = object : CategoryClassificationService {
            override suspend fun classifyCategory(ocrText: String, detectedObjects: List<String>) =
                if (ocrText.startsWith("Bài giảng")) DocumentCategory.STUDY else DocumentCategory.OTHER
        }
        val tagGenerationService = object : TagGenerationService {
            override suspend fun generateTags(ocrText: String, detectedObjects: List<String>) = listOf(ocrText)
        }
        val analyzeDocument = AnalyzeDocument(
            ocrService = FakeOcrService("Bài giảng đại học\nLập chỉ mục cơ sở dữ liệu"),
            objectDetectionService = FakeObjectDetectionService(listOf("book")),
            titleGenerationService = titleGenerationService,
            descriptionGenerationService = descriptionGenerationService,
            categoryClassificationService = categoryClassificationService,
            tagGenerationService = tagGenerationService,
            textTranslationService = FakeTextTranslationService(
                "University lecture\nDatabase indexing",
            ),
        )

        val draft = analyzeDocument("content://vietnamese-lecture")

        assertEquals("Bài giảng đại học\nLập chỉ mục cơ sở dữ liệu", draft.ocrText)
        assertEquals("Bài giảng đại học\nLập chỉ mục cơ sở dữ liệu", draft.title)
        assertEquals("University lecture\nDatabase indexing", draft.description)
        assertEquals(DocumentCategory.STUDY, draft.category)
        assertEquals(listOf("Bài giảng đại học\nLập chỉ mục cơ sở dữ liệu"), draft.tags)
    }

    @Test
    fun saveDocument_savesImage_embedsMetadata_andPersistsDocument() = runBlocking {
        val repository = createRepository()
        val saveDocument = SaveDocument(
            repository = repository,
            imageStorageService = FakeImageStorageService("file://documents/saved.jpg"),
            embeddingService = SimpleEmbeddingService(),
            idGenerator = { "document-id" },
            clock = { 123L },
        )

        val document = saveDocument(createDraft("content://source"))

        assertEquals("document-id", document.id)
        assertEquals("file://documents/saved.jpg", document.imageUri)
        assertEquals(123L, document.createdAt)
        assertTrue(document.embedding.isNotEmpty())
        assertEquals(document, repository.get("document-id"))
    }

    @Test
    fun observeDocuments_returnsNewestDocumentFirst() = runBlocking {
        val repository = createRepository()
        val saveOldDocument = createSaveDocument(repository, "old", 1L)
        val saveNewDocument = createSaveDocument(repository, "new", 2L)

        saveOldDocument(createDraft("content://old", title = "Old document"))
        saveNewDocument(createDraft("content://new", title = "New document"))

        val documents = ObserveDocuments(repository)().first()

        assertEquals(listOf("new", "old"), documents.map { it.id })
    }

    @Test
    fun getDocument_returnsDocumentWithMatchingId() = runBlocking {
        val repository = createRepository()
        createSaveDocument(repository, "wanted", 1L)(createDraft("content://wanted"))

        val document = GetDocument(repository)("wanted")

        assertNotNull(document)
        assertEquals("wanted", document?.id)
    }

    @Test
    fun searchDocuments_ranksMatchingDocumentFirst() = runBlocking {
        val repository = createRepository()
        createSaveDocument(repository, "database", 1L)(
            createDraft("content://database", title = "Database indexing lecture"),
        )
        createSaveDocument(repository, "cooking", 2L)(
            createDraft(
                imageUri = "content://cooking",
                title = "Cooking recipes",
                ocrText = "Recipe ingredients",
                tags = listOf("cooking"),
                detectedObjects = listOf("pan"),
            ),
        )

        val results = SearchDocuments(repository, SimpleEmbeddingService())("database").first()

        assertTrue(results.isNotEmpty())
        assertEquals("database", results.first().document.id)
    }

    private fun createRepository() = DefaultDocumentRepository(FakeDocumentDao(), DocumentMapper())

    private fun createSaveDocument(
        repository: DefaultDocumentRepository,
        id: String,
        createdAt: Long,
    ) = SaveDocument(
        repository = repository,
        imageStorageService = FakeImageStorageService(),
        embeddingService = SimpleEmbeddingService(),
        idGenerator = { id },
        clock = { createdAt },
    )

    private fun createDraft(
        imageUri: String,
        title: String = "Database notes",
        ocrText: String = "Database indexing",
        tags: List<String> = listOf("database"),
        detectedObjects: List<String> = listOf("book"),
    ) = DocumentDraft(
        imageUri = imageUri,
        title = title,
        description = "Study material",
        ocrText = ocrText,
        category = DocumentCategory.STUDY,
        tags = tags,
        detectedObjects = detectedObjects,
    )
}

private class FakeTextTranslationService(
    private val translation: String? = null,
) : TextTranslationService {
    override suspend fun translateToEnglish(text: String) = translation ?: text
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

private class FakeOcrService(private val result: String) : OcrService {
    override suspend fun recognizeText(imageUri: String) = result
}

private class FakeObjectDetectionService(
    private val result: List<String>,
) : ObjectDetectionService {
    override suspend fun detectObjects(imageUri: String) = result
}

private class FakeImageStorageService(
    private val savedUri: String? = null,
) : ImageStorageService {
    override suspend fun saveImageLocally(sourceUri: String) = savedUri ?: sourceUri
}
