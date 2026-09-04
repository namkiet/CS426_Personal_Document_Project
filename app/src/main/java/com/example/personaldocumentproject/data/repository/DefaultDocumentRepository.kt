package com.example.personaldocumentproject.data.repository

import com.example.personaldocumentproject.data.local.DocumentDao
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.SearchResult
import com.example.personaldocumentproject.domain.repository.DocumentRepository
import com.example.personaldocumentproject.domain.service.cosineSimilarity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultDocumentRepository(
    private val documentDao: DocumentDao,
    private val mapper: DocumentMapper,
) : DocumentRepository {
    override fun observeAll(): Flow<List<Document>> =
        documentDao.observeAll().map { entities -> entities.map(mapper::toDomain) }

    override suspend fun get(id: String): Document? = documentDao.get(id)?.let(mapper::toDomain)

    override fun search(query: String, queryEmbedding: List<Float>): Flow<List<SearchResult>> {
        if (query.isBlank()) return observeAll().map { emptyList() }
        val termSeparator = Regex("[^\\p{L}\\p{N}]+")
        val queryTerms = query.lowercase().split(termSeparator).filter(String::isNotBlank).toSet()

        return observeAll().map { documents ->
            documents.map { document ->
                val searchableText = buildString {
                    append(document.title).append(' ')
                    append(document.description).append(' ')
                    append(document.ocrText).append(' ')
                    append(document.tags.joinToString(" "))
                    append(' ').append(document.detectedObjects.joinToString(" "))
                }.lowercase()

                val documentTerms = searchableText.split(termSeparator).filter(String::isNotBlank).toSet()
                val lexicalScore = if (queryTerms.isEmpty()) 0f else
                    queryTerms.count { it in documentTerms }.toFloat() / queryTerms.size

                val semanticScore = cosineSimilarity(queryEmbedding, document.embedding)
                val score = semanticScore * 0.8f + lexicalScore * 0.2f

                SearchResult(document, semanticScore, lexicalScore, score)
            }.filter { it.score >= SEARCH_THRESHOLD || it.lexicalScore > 0f }
             .sortedByDescending(SearchResult::score)
        }
    }

    override suspend fun save(document: Document) = documentDao.upsert(mapper.toEntity(document))

    override suspend fun delete(id: String) = documentDao.deleteById(id)

    companion object {
        const val SEARCH_THRESHOLD = 0.25f
    }
}
