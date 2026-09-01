package com.example.personaldocumentproject.domain.service

interface OcrService {
    suspend fun recognizeText(imageUri: String): String
}
