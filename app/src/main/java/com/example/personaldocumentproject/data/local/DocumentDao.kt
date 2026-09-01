package com.example.personaldocumentproject.data.local

import kotlinx.coroutines.flow.Flow

interface DocumentDao {
    fun observeAll(): Flow<List<DocumentEntity>>
    suspend fun get(id: String): DocumentEntity?
    suspend fun upsert(document: DocumentEntity)
}
