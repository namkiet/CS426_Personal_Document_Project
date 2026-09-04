package com.example.personaldocumentproject.domain.service

import kotlinx.coroutines.flow.Flow

/**
 * Contract for on-device or cloud LLM inference.
 */
interface LlmService {
    /** The human-readable name of the model backing this service. */
    val modelName: String

    /** Whether the model natively supports multimodal image/vision input. */
    val supportsVision: Boolean get() = false

    /**
     * Send [prompt] to the model and collect streamed token chunks as they are generated.
     */
    fun generate(prompt: String): Flow<String>

    /**
     * Send [prompt] inside a stateful multi-turn conversation and collect streamed tokens.
     */
    fun chat(prompt: String): Flow<String>

    /**
     * Send [prompt] along with image bytes and mimeType for multimodal vision inference.
     * Defaults to text-only generate if not overridden.
     */
    suspend fun generateVision(prompt: String, imageBytes: ByteArray, mimeType: String): Flow<String> = generate(prompt)
}
