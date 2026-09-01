package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.OcrService
import com.example.personaldocumentproject.domain.service.ObjectDetectionService
import com.example.personaldocumentproject.domain.service.TitleGenerationService
import com.example.personaldocumentproject.domain.service.DescriptionGenerationService
import com.example.personaldocumentproject.domain.service.CategoryClassificationService
import com.example.personaldocumentproject.domain.service.TagGenerationService

class AnalyzeDocument(
    private val ocrService: OcrService,
    private val objectDetectionService: ObjectDetectionService,
    private val titleGenerationService: TitleGenerationService,
    private val descriptionGenerationService: DescriptionGenerationService,
    private val categoryClassificationService: CategoryClassificationService,
    private val tagGenerationService: TagGenerationService,
) {
    suspend operator fun invoke(imageUri: String): DocumentDraft {
        require(imageUri.isNotBlank()) { "An image URI is required" }

        // 01. Extract text from the selected image using OCR.
        val text = ocrService.recognizeText(imageUri)

        // 02. Detect and label objects found in the selected image.
        val detectedObjects = objectDetectionService.detectObjects(imageUri)

        // 03. Generate editable metadata from the extracted image content.
        val title = titleGenerationService.generateTitle(text, detectedObjects)
        val description = descriptionGenerationService.generateDescription(text, detectedObjects)
        val category = categoryClassificationService.classifyCategory(text, detectedObjects)
        val tags = tagGenerationService.generateTags(text, detectedObjects)

        // 04. Create a document draft from the analysis results.
        val draft = DocumentDraft(
            imageUri = imageUri,
            title = title,
            description = description,
            ocrText = text,
            category = category,
            tags = tags,
            detectedObjects = detectedObjects,
        )

        // 05. Return the analyzed draft to the caller for review.
        return draft
    }
}
