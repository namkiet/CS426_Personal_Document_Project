package com.example.personaldocumentproject.domain.service

interface ImageLabelingService {
    suspend fun generateTags(imageUri: String): List<String>
}