package com.example.personaldocumentproject.domain.model

data class DocumentDraft(
    val imageUri: String,
    val title: String = "",
    val description: String = "",
    val extractedText: String = "",
    val category: DocumentCategory = DocumentCategory.OTHER,
    val tags: List<String> = emptyList(),
    val embedding: List<Float> = emptyList(),
)
