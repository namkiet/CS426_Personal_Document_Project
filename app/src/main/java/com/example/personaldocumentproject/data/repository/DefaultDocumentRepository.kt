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
        val queryTerms = query.lowercase().split(Regex("\\s+")).filter(String::isNotBlank).toSet()

        return observeAll().map { documents ->
            documents.mapNotNull { document ->
                val searchableText = buildString {
                    append(document.title).append(' ')
                    append(document.description).append(' ')
                    append(document.ocrText).append(' ')
                    append(document.tags.joinToString(" "))
                    append(' ').append(document.detectedObjects.joinToString(" "))
                }.lowercase()

                val documentTerms = searchableText.split(Regex("\\s+")).filter(String::isNotBlank).toSet()

                // Calculate Jaccard similarity for text matching
                val textScore = if (queryTerms.isEmpty() || documentTerms.isEmpty()) 0f else {
                    val intersection = queryTerms.intersect(documentTerms).size
                    val union = queryTerms.union(documentTerms).size
                    intersection.toFloat() / union.toFloat()
                }

                // Keep the simple term matching as a boost for direct exact word matches
                val exactMatchBoost = if (queryTerms.isEmpty()) 0f else
                    queryTerms.count(searchableText::contains).toFloat() / queryTerms.size

                // Calculate final text score by combining Jaccard similarity and exact substring matches
                val finalTextScore = maxOf(textScore, exactMatchBoost)

                val semanticScore = cosineSimilarity(queryEmbedding, document.embedding)

                // Give semantic score more weight, but heavily favor if there's a strong text match
                val score = maxOf(finalTextScore * 1.5f, semanticScore).coerceIn(0f, 1f)

                // Adjust threshold depending on similarity
                if (score > 0.1f || finalTextScore > 0f) SearchResult(document, score) else null
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
