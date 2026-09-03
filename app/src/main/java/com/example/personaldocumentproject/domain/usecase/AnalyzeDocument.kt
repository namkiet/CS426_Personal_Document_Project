package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.CategoryClassificationService
import com.example.personaldocumentproject.domain.service.DescriptionGenerationService
import com.example.personaldocumentproject.domain.service.OcrService
import com.example.personaldocumentproject.domain.service.ObjectDetectionService
import com.example.personaldocumentproject.domain.service.TagGenerationService
import com.example.personaldocumentproject.domain.service.TextTranslationService
import com.example.personaldocumentproject.domain.service.TitleGenerationService

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

        // 01. Extract text from the selected image using OCR.
        val recognizedText = ocrService.recognizeText(imageUri)

        // 02. Translate OCR text only for English description generation.
        val translatedText = textTranslationService.translateToEnglish(recognizedText)

        // 03. Detect and label objects found in the selected image.
        val detectedObjects = objectDetectionService.detectObjects(imageUri)

        // 04. Generate editable metadata from the image content.
        val title = titleGenerationService.generateTitle(recognizedText, detectedObjects)
        val description = descriptionGenerationService.generateDescription(translatedText, detectedObjects)
        val category = categoryClassificationService.classifyCategory(recognizedText, detectedObjects)
        val tags = tagGenerationService.generateTags(recognizedText, detectedObjects)

        // 05. Create a document draft from the analysis results.
        val draft = DocumentDraft(
            imageUri = imageUri,
            title = title,
            description = description,
            ocrText = recognizedText,
            category = category,
            tags = tags,
            detectedObjects = detectedObjects,
        )

        // 06. Return the analyzed draft to the caller for review.
        draft
    }
}
