package com.example.personaldocumentproject.data.ml.embedding

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class MediaPipeEmbeddingServiceTest {
    @Test
    fun embedsRelatedTextMoreClosely() = runBlocking {
        val service = MediaPipeEmbeddingService(
            InstrumentationRegistry.getInstrumentation().targetContext,
        )
        val source = service.embed("database indexing lecture")
        val related = service.embed("lesson about database indexes")
        val unrelated = service.embed("banana smoothie recipe")

        assertTrue(source.isNotEmpty())
        assertEquals(source.size, related.size)
        assertTrue(cosine(source, related) > cosine(source, unrelated))
    }

    private fun cosine(first: List<Float>, second: List<Float>): Double =
        first.indices.sumOf { (first[it] * second[it]).toDouble() } /
            sqrt(first.sumOf { (it * it).toDouble() } * second.sumOf { (it * it).toDouble() })
}
