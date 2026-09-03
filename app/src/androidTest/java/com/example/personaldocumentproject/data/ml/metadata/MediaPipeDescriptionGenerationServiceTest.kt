package com.example.personaldocumentproject.data.ml.metadata

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaPipeDescriptionGenerationServiceTest {
    private val service = MediaPipeDescriptionGenerationService(
        InstrumentationRegistry.getInstrumentation().targetContext,
    )

    @Test
    fun generatesTldrFromDocumentContent() = runBlocking {
        val description = service.generateDescription(
            ocrText = """
                Database indexes speed up queries by maintaining an additional data structure.
                They reduce the amount of data a database must scan, but require storage and
                add overhead when rows are inserted or updated.
            """.trimIndent(),
            detectedObjects = listOf("laptop", "book"),
        )

        assertTrue(description.isNotBlank())
        assertNotEquals("Text extracted from the image", description)
    }

    @Test
    fun usesFallbackForEmptyContent() = runBlocking {
        assertEquals("No content detected", service.generateDescription("", emptyList()))
    }
}
