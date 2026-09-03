package com.example.personaldocumentproject.data.ml.metadata

import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.service.CategoryClassificationService
import com.example.personaldocumentproject.domain.service.EmbeddingService
import com.example.personaldocumentproject.domain.service.cosineSimilarity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class EmbeddingCategoryClassificationService(
    private val embeddingService: EmbeddingService,
    private val fallback: CategoryClassificationService = SimpleCategoryClassificationService,
    private val similarityThreshold: Float = 0.30f,
) : CategoryClassificationService {
    init {
        require(similarityThreshold in 0f..1f) { "Similarity threshold must be between 0 and 1" }
    }

    private val cacheMutex = Mutex()
    private var cachedCategoryEmbeddings: Map<DocumentCategory, List<Float>>? = null

    suspend fun prepare() {
        categoryEmbeddings()
    }

    override suspend fun classifyCategory(
        ocrText: String,
        detectedObjects: List<String>,
    ): DocumentCategory {
        val content = listOf(ocrText, detectedObjects.joinToString(" "))
            .filter(String::isNotBlank)
            .joinToString("\n")
        if (content.isBlank()) return fallback.classifyCategory(ocrText, detectedObjects)

        return runCatching {
            val contentEmbedding = embeddingService.embed(content)
            if (contentEmbedding.isEmpty()) return@runCatching fallback.classifyCategory(ocrText, detectedObjects)

            val scores = categoryEmbeddings().mapValues { (_, categoryEmbedding) ->
                cosineSimilarity(contentEmbedding, categoryEmbedding)
            }
            val bestMatch = scores.maxBy { it.value }
            if (bestMatch.value >= similarityThreshold) bestMatch.key else DocumentCategory.OTHER
        }.getOrElse { error ->
            if (error is CancellationException) throw error
            fallback.classifyCategory(ocrText, detectedObjects)
        }
    }

    private suspend fun categoryEmbeddings(): Map<DocumentCategory, List<Float>> = cacheMutex.withLock {
        cachedCategoryEmbeddings ?: categoryDescriptions.mapValues { (_, description) ->
            embeddingService.embed(description)
        }.also { cachedCategoryEmbeddings = it }
    }

    private companion object {
        val categoryDescriptions = mapOf(
            DocumentCategory.NOTE to "personal note, memo or reminder",
            DocumentCategory.RECEIPT to "receipt, invoice, purchase, payment, subtotal, or total",
            DocumentCategory.IDENTITY to "passport, driver license, or citizen card",
            DocumentCategory.STUDY to "study material, lecture, course, school, university, exam or textbook",
            DocumentCategory.WORK to "meeting, project, company, business, or contract",
        )
    }
}
