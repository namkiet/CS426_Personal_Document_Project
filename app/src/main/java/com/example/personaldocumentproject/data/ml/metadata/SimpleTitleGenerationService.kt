package com.example.personaldocumentproject.data.ml.metadata

import com.example.personaldocumentproject.domain.service.TitleGenerationService

class SimpleTitleGenerationService : TitleGenerationService {
    override suspend fun generateTitle(ocrText: String, detectedObjects: List<String>): String =
        ocrText.lineSequence().firstOrNull { it.isNotBlank() }?.take(80)
            ?: detectedObjects.firstOrNull()?.replaceFirstChar(Char::uppercase)
            ?: "New document"
}
