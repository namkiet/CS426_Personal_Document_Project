package com.example.personaldocumentproject.data.ml.embedding

import android.content.Context
import com.example.personaldocumentproject.domain.service.EmbeddingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

/**
 * Pure Kotlin Local Embedding Service.
 * Generates deterministic normalized feature vectors using word & character n-gram hashing.
 * Safe from native C++ SIGSEGV crashes across all device architectures.
 */
class LocalEmbeddingService(
    @Suppress("UNUSED_PARAMETER") context: Context,
) : EmbeddingService {

    companion object {
        private const val VECTOR_SIZE = 64
    }

    override suspend fun embed(text: String): List<Float> = withContext(Dispatchers.Default) {
        val trimmed = text.trim().lowercase()
        if (trimmed.isBlank()) return@withContext emptyList()

        val vector = FloatArray(VECTOR_SIZE)
        val tokens = trimmed.split(Regex("\\s+")).filter(String::isNotBlank)

        for (token in tokens) {
            // Word level hashing
            val wordBucket = (token.hashCode() and 0x7FFFFFFF) % VECTOR_SIZE
            vector[wordBucket] += 2.0f

            // Character 3-gram hashing for partial subword matching
            if (token.length >= 3) {
                for (i in 0..(token.length - 3)) {
                    val gram = token.substring(i, i + 3)
                    val gramBucket = (gram.hashCode() and 0x7FFFFFFF) % VECTOR_SIZE
                    vector[gramBucket] += 1.0f
                }
            }
        }

        // L2 normalization
        var sumSquares = 0.0f
        for (v in vector) {
            sumSquares += v * v
        }

        val magnitude = sqrt(sumSquares)
        if (magnitude > 0.0f) {
            for (i in vector.indices) {
                vector[i] /= magnitude
            }
        }

        vector.toList()
    }
}
