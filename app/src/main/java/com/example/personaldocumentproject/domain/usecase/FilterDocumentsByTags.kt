package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.Document

class FilterDocumentsByTags {
    operator fun invoke(documents: List<Document>, selectedTags: Set<String>): List<Document> {
        if (selectedTags.isEmpty()) return documents
        return documents.filter { doc ->
            selectedTags.all { selectedTag ->
                doc.tags.any { it.equals(selectedTag, ignoreCase = true) }
            }
        }
    }
}
