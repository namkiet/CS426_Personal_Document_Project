package com.example.personaldocumentproject.data.ml.metadata

import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.service.CategoryClassificationService

object SimpleCategoryClassificationService : CategoryClassificationService {
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
}
