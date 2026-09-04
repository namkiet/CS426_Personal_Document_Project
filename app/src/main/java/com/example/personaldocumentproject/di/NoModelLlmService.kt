package com.example.personaldocumentproject.di

import com.example.personaldocumentproject.domain.service.LlmService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Fails clearly when AI enhancement is requested before a model is installed. */
internal object NoModelLlmService : LlmService {
    override val modelName: String = "No model"

    private fun unavailable(): Flow<String> = flow {
        error("No AI model is downloaded. Open Model Manager and download a model first.")
    }

    override fun generate(prompt: String): Flow<String> = unavailable()
    override fun chat(prompt: String): Flow<String> = unavailable()
}
