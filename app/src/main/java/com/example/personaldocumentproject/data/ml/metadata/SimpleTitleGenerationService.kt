package com.example.personaldocumentproject.data.ml.metadata

import com.example.personaldocumentproject.domain.service.TitleGenerationService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SimpleTitleGenerationService : TitleGenerationService {
    override suspend fun generateTitle(ocrText: String, detectedObjects: List<String>): String =
        ocrText.lineSequence()
            .map(String::trim)
            .firstOrNull(String::isNotBlank)
            ?.take(MAX_TITLE_LENGTH)
            ?: detectedObjects.firstOrNull()?.trim()?.takeIf(String::isNotBlank)
            ?: "New document - ${SimpleDateFormat(TIMESTAMP_PATTERN, Locale.getDefault()).format(Date())}"

    private const val TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss"
    private const val MAX_TITLE_LENGTH = 80
}
