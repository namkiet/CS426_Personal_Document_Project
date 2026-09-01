package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.SearchResult
import com.example.personaldocumentproject.domain.repository.DocumentRepository
import com.example.personaldocumentproject.domain.service.EmbeddingService
import kotlinx.coroutines.flow.Flow

class SearchDocuments(
    private val repository: DocumentRepository,
    private val embeddingService: EmbeddingService,
) {
    suspend operator fun invoke(query: String): Flow<List<SearchResult>> {
        val normalizedQuery = query.trim()
        return repository.search(normalizedQuery, embeddingService.embed(normalizedQuery))
    }
}
