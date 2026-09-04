package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.repository.DocumentRepository
import com.example.personaldocumentproject.domain.repository.SettingRepository
import com.example.personaldocumentproject.domain.service.ImageStorageService
import kotlinx.coroutines.flow.first

class ResetApplicationData(
    private val documentRepository: DocumentRepository,
    private val imageStorageService: ImageStorageService,
    private val settingRepository: SettingRepository,
) {
    suspend operator fun invoke() {
        val documents = documentRepository.observeAll().first()

        try {
            documentRepository.deleteAll()
            imageStorageService.clearAllImages()
            settingRepository.clearSettings()
        } catch (error: Throwable) {
            documents.forEach { document ->
                runCatching { documentRepository.save(document) }
            }
            throw error
        }
    }
}
