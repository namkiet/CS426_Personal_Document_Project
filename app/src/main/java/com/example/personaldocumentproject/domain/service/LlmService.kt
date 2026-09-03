package com.example.personaldocumentproject.domain.service

import kotlinx.coroutines.flow.Flow

/**
 * Contract for on-device LLM inference.
 *
 * Implementations wrap a specific LiteRT-LM model (e.g. Gemma-4-E2B, Qwen3-0.6B) and
 * stream generated tokens back to the caller as a [Flow].
 */
interface LlmService {
    /** The human-readable name of the model backing this service. */
    val modelName: String

    /**
     * Send [prompt] to the model and collect streamed token chunks as they are generated.
     * Each emitted [String] is a partial token piece, not a full sentence.
     */
    fun generate(prompt: String): Flow<String>

    /**
     * Send [prompt] inside a stateful multi-turn conversation and collect streamed tokens.
     * Callers are responsible for managing turn history in the prompt if the underlying
     * model does not maintain state between calls.
     */
    fun chat(prompt: String): Flow<String>
}
