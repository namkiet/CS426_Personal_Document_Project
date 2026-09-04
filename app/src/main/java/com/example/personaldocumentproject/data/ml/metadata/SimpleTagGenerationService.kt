package com.example.personaldocumentproject.data.ml.metadata

import com.example.personaldocumentproject.domain.service.TagGenerationService

object SimpleTagGenerationService : TagGenerationService {

    private val STOP_WORDS = setOf(
        "the", "and", "for", "with", "this", "that", "from", "have", "are", "was", "were",
        "been", "being", "will", "would", "shall", "should", "can", "could", "about",
        "into", "through", "after", "before", "under", "over", "between", "some", "such",
        "của", "và", "là", "các", "trong", "cho", "với", "ngày", "tháng", "năm", "tại",
        "theo", "được", "trên", "khi", "như", "này", "đến", "những", "nhiều", "không", "tổng",
    )

    private val DOMAIN_KEYWORDS = setOf(
        "receipt", "invoice", "bill", "tax", "statement", "payment", "order",
        "passport", "identity", "license", "certificate", "contract", "agreement", "visa", "ticket",
        "lecture", "course", "syllabus", "assignment", "homework", "exam", "grade",
        "meeting", "report", "project", "schedule", "proposal", "memo", "document",
    )

    override suspend fun generateTags(ocrText: String, detectedObjects: List<String>): List<String> {
        val objectTags = detectedObjects
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        val textTokens = ocrText
            .lowercase()
            .split(Regex("[^\\p{L}\\p{Nd}-]+"))
            .filter { token ->
                token.length in 3..25 &&
                    token !in STOP_WORDS &&
                    !token.all(Char::isDigit)
            }

        // Prioritize domain keywords found in text
        val priorityTags = textTokens
            .filter { it in DOMAIN_KEYWORDS }
            .distinct()

        // Extract frequent meaningful words from text
        val frequentWords = textTokens
            .filter { it !in DOMAIN_KEYWORDS }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }

        return (objectTags + priorityTags + frequentWords)
            .distinct()
            .take(MAX_TAGS)
    }

    private const val MAX_TAGS = 5
}
