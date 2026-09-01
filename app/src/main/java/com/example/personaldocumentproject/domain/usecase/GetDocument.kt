package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.repository.DocumentRepository

class GetDocument(private val repository: DocumentRepository) {
    suspend operator fun invoke(id: String) = repository.get(id)
}
