package com.example.personaldocumentproject.domain.service

interface EmbeddingService {
    suspend fun embed(text: String): List<Float>
}

fun cosineSimilarity(first: List<Float>, second: List<Float>): Float {
    if (first.isEmpty() || first.size != second.size) return 0f
    val dot = first.indices.sumOf { (first[it] * second[it]).toDouble() }
    val firstMagnitude = kotlin.math.sqrt(first.sumOf { (it * it).toDouble() })
    val secondMagnitude = kotlin.math.sqrt(second.sumOf { (it * it).toDouble() })
    return if (firstMagnitude == 0.0 || secondMagnitude == 0.0) 0f
    else (dot / (firstMagnitude * secondMagnitude)).toFloat().coerceIn(0f, 1f)
}
