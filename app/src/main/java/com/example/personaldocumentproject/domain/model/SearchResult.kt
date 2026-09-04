package com.example.personaldocumentproject.domain.model

data class SearchResult(
    val document: Document,
    val semanticScore: Float,
    val lexicalScore: Float,
    val score: Float,
)
