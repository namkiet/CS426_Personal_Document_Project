package com.example.personaldocumentproject.data.ml.metadata

import android.content.Context
import com.example.personaldocumentproject.domain.service.DescriptionGenerationService
import com.google.mediapipe.tasks.text.textsummarizer.TextSummarizer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MediaPipeDescriptionGenerationService(
    context: Context,
) : DescriptionGenerationService {
    private val applicationContext = context.applicationContext

    suspend fun prepare() {
        withContext(Dispatchers.IO) { copyModelToCache() }
    }

    override suspend fun generateDescription(ocrText: String, detectedObjects: List<String>): String {
        val objects = detectedObjects.map(String::trim).filter(String::isNotBlank).distinct()
        val input = listOf(
            ocrText.trim(),
            objects.takeIf(List<String>::isNotEmpty)?.joinToString(", ", "Detected objects: ").orEmpty(),
        ).filter(String::isNotBlank).joinToString("\n\n")

        if (input.isBlank()) return fallbackDescription(ocrText, detectedObjects)

        return runCatching {
            prepare()
            val modelPath = File(applicationContext.cacheDir, MODEL_PATH).absolutePath
            withContext(Dispatchers.Default) {
                val options = TextSummarizer.TextSummarizerOptions.builder()
                    .setModelPath(modelPath)
                    .setMode(TextSummarizer.TextSummarizerOptions.Mode.TLDR)
                    .setMaxNumTokens(MAX_NUM_TOKENS)
                    .build()
                TextSummarizer.createFromOptions(applicationContext, options).use {
                    it.summarize(input).summary.trim()
                }
            }.ifBlank { fallbackDescription(ocrText, detectedObjects) }
        }.getOrElse { error ->
            if (error is CancellationException) throw error
            fallbackDescription(ocrText, detectedObjects)
        }
    }

    private fun fallbackDescription(ocrText: String, detectedObjects: List<String>): String = when {
        ocrText.isNotBlank() -> ocrText.lineSequence().firstOrNull { it.isNotBlank() }?.take(100)?.trim().orEmpty()
        detectedObjects.isNotEmpty() -> detectedObjects.joinToString(", ")
        else -> ""
    }

    private fun copyModelToCache(): String {
        val destination = File(applicationContext.cacheDir, MODEL_PATH)
        val assetSize = applicationContext.assets.openFd(MODEL_PATH).use { it.length }
        if (destination.length() != assetSize) {
            applicationContext.assets.open(MODEL_PATH).use { input ->
                FileOutputStream(destination).use { output -> input.copyTo(output) }
            }
        }
        return destination.absolutePath
    }

    private companion object {
        const val MODEL_PATH = "summarization_quant_200m_2modes.litertlm"
        const val MAX_NUM_TOKENS = 2_048
    }
}
