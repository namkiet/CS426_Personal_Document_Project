package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow

class ObserveDocuments(private val repository: DocumentRepository) {
    operator fun invoke(): Flow<List<Document>> = repository.observeAll()
}
