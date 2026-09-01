package com.example.personaldocumentproject.data.repository

import com.example.personaldocumentproject.data.local.DocumentDao
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.SearchResult
import com.example.personaldocumentproject.domain.repository.DocumentRepository
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
        val terms = query.lowercase().split(Regex("\\s+")).filter(String::isNotBlank)
        return observeAll().map { documents ->
            documents.mapNotNull { document ->
                val searchableText = buildString {
                    append(document.title).append(' ')
                    append(document.description).append(' ')
                    append(document.extractedText).append(' ')
                    append(document.tags.joinToString(" "))
                }.lowercase()
                val textScore = if (terms.isEmpty()) 1f else
                    terms.count(searchableText::contains).toFloat() / terms.size
                val semanticScore = cosineSimilarity(queryEmbedding, document.embedding)
                val score = maxOf(textScore, semanticScore)
                if (score > 0f) SearchResult(document, score) else null
            }.sortedByDescending(SearchResult::score)
        }
    }

    override suspend fun save(document: Document) = documentDao.upsert(mapper.toEntity(document))

    private fun cosineSimilarity(first: List<Float>, second: List<Float>): Float {
        if (first.isEmpty() || first.size != second.size) return 0f
        val dot = first.indices.sumOf { (first[it] * second[it]).toDouble() }
        val firstMagnitude = kotlin.math.sqrt(first.sumOf { (it * it).toDouble() })
        val secondMagnitude = kotlin.math.sqrt(second.sumOf { (it * it).toDouble() })
        return if (firstMagnitude == 0.0 || secondMagnitude == 0.0) 0f
        else (dot / (firstMagnitude * secondMagnitude)).toFloat().coerceIn(0f, 1f)
    }
}
