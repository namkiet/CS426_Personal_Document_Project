package com.example.personaldocumentproject.domain.service

interface TagGenerationService {
    suspend fun generateTags(ocrText: String, detectedObjects: List<String>): List<String>
}
