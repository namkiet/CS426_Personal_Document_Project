package com.example.personaldocumentproject.data.local

data class DocumentEntity(
    val id: String,
    val imageUri: String,
    val title: String,
    val description: String,
    val extractedText: String,
    val category: String,
    val tags: String,
    val embedding: String,
    val createdAtEpochMillis: Long,
)
