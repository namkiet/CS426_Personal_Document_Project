package com.example.personaldocumentproject.di

import com.example.personaldocumentproject.domain.service.LlmService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Stub [LlmService] used when no model file is present on disk.
 * Returns a clear error message so the UI can surface it rather than crashing.
 */
internal object NoModelLlmService : LlmService {
    override val modelName: String = "No model"

    private val message = flowOf(
        "⚠️ No AI model is downloaded yet.\n" +
        "Go to Library → Manage AI Models to download one."
    )

    override fun generate(prompt: String): Flow<String> = message
    override fun chat(prompt: String): Flow<String> = message
}
