package com.example.personaldocumentproject.di

import com.example.personaldocumentproject.domain.service.LlmService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Stub [LlmService] used when "No model" is selected or no model file is present on disk.
 */
internal object NoModelLlmService : LlmService {
    override val modelName: String = "No model"
    override val supportsVision: Boolean = false

    private fun unavailable(): Flow<String> = flow {
        error("No AI model is configured.")
    }

    override fun generate(prompt: String): Flow<String> = unavailable()
    override fun chat(prompt: String): Flow<String> = unavailable()
    override suspend fun generateVision(prompt: String, imageBytes: ByteArray, mimeType: String): Flow<String> = unavailable()
}
