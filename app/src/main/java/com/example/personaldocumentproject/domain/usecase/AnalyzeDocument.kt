package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.CategoryClassificationService
import com.example.personaldocumentproject.domain.service.DescriptionGenerationService
import com.example.personaldocumentproject.domain.service.OcrService
import com.example.personaldocumentproject.domain.service.ObjectDetectionService
import com.example.personaldocumentproject.domain.service.TagGenerationService
import com.example.personaldocumentproject.domain.service.TextTranslationService
import com.example.personaldocumentproject.domain.service.TitleGenerationService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class AnalyzeDocument(
    private val ocrService: OcrService,
    private val objectDetectionService: ObjectDetectionService,
    private val titleGenerationService: TitleGenerationService,
    private val descriptionGenerationService: DescriptionGenerationService,
    private val categoryClassificationService: CategoryClassificationService,
    private val tagGenerationService: TagGenerationService,
    private val textTranslationService: TextTranslationService,
) {
    suspend operator fun invoke(imageUri: String): DocumentDraft = coroutineScope {
        require(imageUri.isNotBlank()) { "An image URI is required" }

        // 01. Extract text and detect objects in parallel.
        val recognizedTextDeferred = async { ocrService.recognizeText(imageUri) }
        val detectedObjectsDeferred = async { objectDetectionService.detectObjects(imageUri) }
        val recognizedText = recognizedTextDeferred.await()
        val detectedObjects = detectedObjectsDeferred.await()

        // 02. Translate once, then generate all metadata from the translated text.
        val translatedText = textTranslationService.translateToEnglish(recognizedText)

        // 03. Generate independent metadata in parallel.
        val titleDeferred = async {
            titleGenerationService.generateTitle(translatedText, detectedObjects)
        }
        val categoryDeferred = async {
            categoryClassificationService.classifyCategory(translatedText, detectedObjects)
        }
        val tagsDeferred = async {
            tagGenerationService.generateTags(translatedText, detectedObjects)
        }
        val descriptionDeferred = async {
            descriptionGenerationService.generateDescription(translatedText, detectedObjects)
        }

        // 04. Create a document draft from the analysis results.
        DocumentDraft(
            imageUri = imageUri,
            title = titleDeferred.await(),
            description = descriptionDeferred.await(),
            ocrText = recognizedText,
            category = categoryDeferred.await(),
            tags = tagsDeferred.await(),
            detectedObjects = detectedObjects,
        )
    }
}
