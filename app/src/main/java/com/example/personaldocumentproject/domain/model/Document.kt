package com.example.personaldocumentproject.domain.model

data class Document(
    val id: String,
    val imageUri: String,
    val title: String,
    val description: String,
    val ocrText: String,
    val category: DocumentCategory,
    val tags: List<String>,
    val detectedObjects: List<String>,
    val embedding: List<Float>,
    val createdAt: Long,
)
