package com.example.personaldocumentproject.di

import com.example.personaldocumentproject.domain.service.LlmService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Stub [LlmService] used when "No model" is selected or no model file is present on disk.
 */
internal object NoModelLlmService : LlmService {
    override val modelName: String = "No model"
    override val supportsVision: Boolean = false

    private val message = flowOf(
        "⚠️ No AI model is selected or downloaded.\n" +
        "Go to Library → Manage AI Models to change your selection."
    )

    override fun generate(prompt: String): Flow<String> = message
    override fun chat(prompt: String): Flow<String> = message
}
