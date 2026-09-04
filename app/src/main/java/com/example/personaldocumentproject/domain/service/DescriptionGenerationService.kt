package com.example.personaldocumentproject.domain.service

interface DescriptionGenerationService {
    suspend fun generateDescription(ocrText: String, detectedObjects: List<String>): String
}
