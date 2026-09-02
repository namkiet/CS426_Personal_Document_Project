package com.example.personaldocumentproject.data.ml.metadata

import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.service.MetadataService

class SimpleMetadataService : MetadataService {
    override suspend fun generateTitle(ocrText: String, detectedObjects: List<String>): String =
        ocrText.lineSequence().firstOrNull { it.isNotBlank() }?.take(80)
            ?: detectedObjects.firstOrNull()?.replaceFirstChar(Char::uppercase)
            ?: "New document"

    override suspend fun generateDescription(ocrText: String, detectedObjects: List<String>): String = when {
        ocrText.isNotBlank() -> "Text extracted from the image"
        detectedObjects.isNotEmpty() -> "Objects detected in the image"
        else -> "No content detected"
    }

    override suspend fun classifyCategory(
        ocrText: String,
        detectedObjects: List<String>,
    ): DocumentCategory {
        val content = (ocrText + " " + detectedObjects.joinToString(" ")).lowercase()
        return when {
            listOf("receipt", "invoice", "total", "subtotal").any(content::contains) -> DocumentCategory.RECEIPT
            listOf("identity", "passport", "driver license", "citizen").any(content::contains) -> DocumentCategory.IDENTITY
            listOf("lecture", "course", "student", "school", "university").any(content::contains) -> DocumentCategory.STUDY
            listOf("meeting", "project", "company", "contract").any(content::contains) -> DocumentCategory.WORK
            listOf("note", "memo").any(content::contains) -> DocumentCategory.NOTE
            else -> DocumentCategory.OTHER
        }
    }

    override suspend fun generateTags(ocrText: String, detectedObjects: List<String>): List<String> =
        detectedObjects.map(String::trim).filter(String::isNotBlank).distinct()
}
