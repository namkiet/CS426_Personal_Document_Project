package com.example.personaldocumentproject.data.ml.embedding

import com.example.personaldocumentproject.domain.service.EmbeddingService

/** Local model integration point. The scaffold uses a deterministic lightweight vector. */
class SimpleEmbeddingService : EmbeddingService {
    override suspend fun embed(text: String): List<Float> {
        if (text.isBlank()) return emptyList()
        val vector = FloatArray(16)
        text.lowercase().forEach { character ->
            vector[character.code % vector.size] += 1f
        }
        val magnitude = kotlin.math.sqrt(vector.sumOf { (it * it).toDouble() }).toFloat()
        return vector.map { if (magnitude == 0f) 0f else it / magnitude }
    }
}
