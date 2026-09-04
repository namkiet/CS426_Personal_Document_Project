package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.Document

class ExtractTagsFromDocuments {
    operator fun invoke(
        documents: List<Document>,
        additionalTags: List<String> = emptyList()
    ): List<String> {
        val documentTags = documents.flatMap { it.tags }
        return (documentTags + additionalTags)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
}
