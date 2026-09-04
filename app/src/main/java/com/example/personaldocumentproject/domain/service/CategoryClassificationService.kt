package com.example.personaldocumentproject.domain.service

import com.example.personaldocumentproject.domain.model.DocumentCategory

interface CategoryClassificationService {
    suspend fun classifyCategory(ocrText: String, detectedObjects: List<String>): DocumentCategory
}
