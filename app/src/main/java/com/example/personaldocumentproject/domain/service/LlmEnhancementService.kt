package com.example.personaldocumentproject.domain.service

interface LlmEnhancementService {
    /** Prompts the LLM to clean up the OCR text and image labels to create a coherent text block. */
    suspend fun enhanceDocumentData(ocrText: String, imageLabels: List<String>, languageHint: String? = null): String
}