package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.repository.DocumentRepository
import com.example.personaldocumentproject.domain.repository.SettingRepository
import com.example.personaldocumentproject.domain.service.ImageStorageService

class ResetApplicationData(
    private val documentRepository: DocumentRepository,
    private val imageStorageService: ImageStorageService,
    private val settingRepository: SettingRepository,
) {
    suspend operator fun invoke() {
        documentRepository.deleteAll()
        imageStorageService.clearAllImages()
        settingRepository.clearSettings()
    }
}
