package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.repository.DocumentRepository
import com.example.personaldocumentproject.domain.service.ImageStorageService

class DeleteDocument(
    private val repository: DocumentRepository,
    private val imageStorageService: ImageStorageService,
) {
    suspend operator fun invoke(id: String) {
        val document = repository.get(id) ?: return

        try {
            repository.delete(id)
            imageStorageService.deleteImage(document.imageUri)
        } catch (error: Throwable) {
            runCatching { repository.save(document) }
            throw error
        }
    }
}
