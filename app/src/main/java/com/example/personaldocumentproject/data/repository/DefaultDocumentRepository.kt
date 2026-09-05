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
                val lexicalScore = if (queryTerms.isEmpty()) {
                    0f
                } else {
                    queryTerms.map { queryTerm ->
                        documentTerms.maxOfOrNull { documentTerm ->
                            fuzzySimilarity(queryTerm, documentTerm)
                        } ?: 0f
                    }.average().toFloat()
                }

                val semanticScore = cosineSimilarity(queryEmbedding, document.embedding)
                val score = semanticScore * 0.8f + lexicalScore * 0.2f

                SearchResult(document, semanticScore, lexicalScore, score)
            }.filter { it.score >= SEARCH_THRESHOLD || it.lexicalScore > 0f }
             .sortedByDescending(SearchResult::score)
        }
    }

    override suspend fun save(document: Document) = documentDao.upsert(mapper.toEntity(document))

    override suspend fun delete(id: String) = documentDao.deleteById(id)

    override suspend fun deleteAll() = documentDao.deleteAll()

    companion object {
        const val SEARCH_THRESHOLD = 0.25f
    }
}

fun fuzzySimilarity(a: String, b: String): Float {
    val distance = levenshteinDistance(a, b)
    val maxLength = maxOf(a.length, b.length)

    if (maxLength == 0) return 1f

    return 1f - distance.toFloat() / maxLength
}

fun levenshteinDistance(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length

    val dp = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        var prev = dp[0]
        dp[0] = i
        for (j in 1..b.length) {
            val temp = dp[j]
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            dp[j] = minOf(
                dp[j] + 1,
                dp[j - 1] + 1,
                prev + cost
            )
            prev = temp
        }
    }
    return dp[b.length]
}
