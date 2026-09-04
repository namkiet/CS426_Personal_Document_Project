package com.example.personaldocumentproject.data.ml.embedding

import android.content.Context
import com.example.personaldocumentproject.domain.service.EmbeddingService
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaPipeEmbeddingService(context: Context) : EmbeddingService {
    private val applicationContext = context.applicationContext
    private val embedder by lazy {
        val options = TextEmbedder.TextEmbedderOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath(MODEL_PATH).build())
            .build()
        TextEmbedder.createFromOptions(applicationContext, options)
    }

    override suspend fun embed(text: String): List<Float> {
        if (text.isBlank()) return emptyList()
        return withContext(Dispatchers.Default) {
            synchronized(embedder) {
                embedder.embed(text).embeddingResult().embeddings().first().floatEmbedding().toList()
            }
        }
    }

    private companion object {
        const val MODEL_PATH = "universal_sentence_encoder.tflite"
    }
}
