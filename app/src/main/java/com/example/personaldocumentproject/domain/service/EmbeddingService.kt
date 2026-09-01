package com.example.personaldocumentproject.domain.service

interface EmbeddingService {
    suspend fun embed(text: String): List<Float>
}
