package com.example.personaldocumentproject.data.ml.metadata

import com.example.personaldocumentproject.domain.service.DescriptionGenerationService

object SimpleDescriptionGenerationService : DescriptionGenerationService {
    override suspend fun generateDescription(ocrText: String, detectedObjects: List<String>): String = when {
        ocrText.isNotBlank() -> "Text extracted from the image"
        detectedObjects.isNotEmpty() -> "Objects detected in the image"
        else -> "No content detected"
    }
}
