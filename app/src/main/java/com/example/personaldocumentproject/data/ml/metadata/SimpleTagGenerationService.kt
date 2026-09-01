package com.example.personaldocumentproject.data.ml.metadata

import com.example.personaldocumentproject.domain.service.TagGenerationService

class SimpleTagGenerationService : TagGenerationService {
    override suspend fun generateTags(ocrText: String, detectedObjects: List<String>): List<String> =
        detectedObjects.map(String::trim).filter(String::isNotBlank).distinct()
}
