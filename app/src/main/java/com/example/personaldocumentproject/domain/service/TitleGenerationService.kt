package com.example.personaldocumentproject.domain.service

interface TitleGenerationService {
    suspend fun generateTitle(ocrText: String, detectedObjects: List<String>): String
}
