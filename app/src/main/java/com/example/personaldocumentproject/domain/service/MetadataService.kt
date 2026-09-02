package com.example.personaldocumentproject.domain.service

import com.example.personaldocumentproject.domain.model.DocumentCategory

interface MetadataService {
    suspend fun generateTitle(ocrText: String, detectedObjects: List<String>): String
    suspend fun generateDescription(ocrText: String, detectedObjects: List<String>): String
    suspend fun classifyCategory(ocrText: String, detectedObjects: List<String>): DocumentCategory
    suspend fun generateTags(ocrText: String, detectedObjects: List<String>): List<String>
}
