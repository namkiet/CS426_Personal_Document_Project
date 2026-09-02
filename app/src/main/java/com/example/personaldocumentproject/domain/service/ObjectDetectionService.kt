package com.example.personaldocumentproject.domain.service

interface ObjectDetectionService {
    suspend fun detectObjects(imageUri: String): List<String>
}
