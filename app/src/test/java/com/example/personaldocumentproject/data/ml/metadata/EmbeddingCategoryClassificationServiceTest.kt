package com.example.personaldocumentproject.data.ml.metadata

import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.service.EmbeddingService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class EmbeddingCategoryClassificationServiceTest {
    private val service = EmbeddingCategoryClassificationService(object : EmbeddingService {
        override suspend fun embed(text: String): List<Float> =
            if ("receipt" in text || "payment total" in text) listOf(1f, 0f) else listOf(0f, 1f)
    })

    @Test
    fun selectsCategoryWithHighestEmbeddingSimilarity() = runBlocking {
        assertEquals(
            DocumentCategory.RECEIPT,
            service.classifyCategory("Store payment total", listOf("paper")),
        )
    }

    @Test
    fun returnsOtherForEmptyContent() = runBlocking {
        assertEquals(DocumentCategory.OTHER, service.classifyCategory("", emptyList()))
    }

    @Test
    fun usesKeywordFallbackWhenEmbeddingFails() = runBlocking {
        val failingService = EmbeddingCategoryClassificationService(object : EmbeddingService {
            override suspend fun embed(text: String): List<Float> = error("Model unavailable")
        })

        assertEquals(
            DocumentCategory.STUDY,
            failingService.classifyCategory("University lecture", emptyList()),
        )
    }

    @Test
    fun cachesCategoryEmbeddingsBetweenDocuments() = runBlocking {
        var embeddingCalls = 0
        val cachingService = EmbeddingCategoryClassificationService(object : EmbeddingService {
            override suspend fun embed(text: String): List<Float> {
                embeddingCalls++
                return listOf(1f, 0f)
            }
        })

        cachingService.classifyCategory("First document", emptyList())
        cachingService.classifyCategory("Second document", emptyList())

        assertEquals(7, embeddingCalls)
    }

    @Test
    fun returnsOtherWhenEverySimilarityIsBelowThreshold() = runBlocking {
        val thresholdService = EmbeddingCategoryClassificationService(
            embeddingService = object : EmbeddingService {
                override suspend fun embed(text: String): List<Float> =
                    if (text == "Unrelated content") listOf(1f, 0f) else listOf(0f, 1f)
            },
            similarityThreshold = 0.30f,
        )

        assertEquals(
            DocumentCategory.OTHER,
            thresholdService.classifyCategory("Unrelated content", emptyList()),
        )
    }
}
