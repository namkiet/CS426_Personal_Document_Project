package com.example.personaldocumentproject.domain.repository

import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeAll(): Flow<List<Document>>
    suspend fun get(id: String): Document?
    fun search(query: String, queryEmbedding: List<Float>): Flow<List<SearchResult>>
    suspend fun save(document: Document)
}
