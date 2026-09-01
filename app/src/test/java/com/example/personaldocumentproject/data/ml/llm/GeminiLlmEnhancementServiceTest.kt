package com.example.personaldocumentproject.data.ml.llm

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiLlmEnhancementServiceTest {

    @Test
    fun enhanceDocumentData_whenFirebaseUninitialized_fallsBackToOcrText() = runBlocking {
        val service = GeminiLlmEnhancementService()
        val result = service.enhanceDocumentData(
            ocrText = "Sample document text",
            imageLabels = listOf("document", "text"),
            languageHint = null
        )

        assertEquals("Sample document text", result)
    }
}
