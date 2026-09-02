package com.example.personaldocumentproject.data.ml.embedding

import android.content.Context
import com.example.personaldocumentproject.domain.service.EmbeddingService
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextEmbedderOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** True offline Semantic Embedding using Google MediaPipe and Universal Sentence Encoder */
class LocalEmbeddingService(context: Context) : EmbeddingService {

    private val embedder: TextEmbedder? by lazy {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("universal_sentence_encoder.tflite")
                .build()

            val options = TextEmbedderOptions.builder()
                .setBaseOptions(baseOptions)
                .build()

            TextEmbedder.createFromOptions(context, options)
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    override suspend fun embed(text: String): List<Float> = withContext(Dispatchers.Default) {
        if (text.isBlank()) return@withContext emptyList()

        try {
            val instance = embedder ?: return@withContext emptyList()
            val result = instance.embed(text)

            // MediaPipe returns a list of embeddings. We only asked to embed one text.
            val embedding = result.embeddingResult().embeddings().firstOrNull()

            // Convert to a List<Float> for our domain layer
            embedding?.floatEmbedding()?.toList() ?: emptyList()
        } catch (t: Throwable) {
            t.printStackTrace()
            emptyList()
        }
    }
}
