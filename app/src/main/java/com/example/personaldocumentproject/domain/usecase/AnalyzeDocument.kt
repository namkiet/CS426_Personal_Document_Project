package com.example.personaldocumentproject.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.personaldocumentproject.data.ml.llm.LlmDocumentAnalyzer
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.CategoryClassificationService
import com.example.personaldocumentproject.domain.service.DescriptionGenerationService
import com.example.personaldocumentproject.domain.service.LlmService
import com.example.personaldocumentproject.domain.service.OcrService
import com.example.personaldocumentproject.domain.service.ObjectDetectionService
import com.example.personaldocumentproject.domain.service.TagGenerationService
import com.example.personaldocumentproject.domain.service.TextTranslationService
import com.example.personaldocumentproject.domain.service.TitleGenerationService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList

class AnalyzeDocument(
    private val context: Context? = null,
    private val ocrService: OcrService,
    private val objectDetectionService: ObjectDetectionService,
    private val titleGenerationService: TitleGenerationService,
    private val descriptionGenerationService: DescriptionGenerationService,
    private val categoryClassificationService: CategoryClassificationService,
    private val tagGenerationService: TagGenerationService,
    private val textTranslationService: TextTranslationService,
    private val geminiService: LlmService? = null,
) {
    // Secondary constructor for backward compatibility with unit tests
    constructor(
        ocrService: OcrService,
        objectDetectionService: ObjectDetectionService,
        titleGenerationService: TitleGenerationService,
        descriptionGenerationService: DescriptionGenerationService,
        categoryClassificationService: CategoryClassificationService,
        tagGenerationService: TagGenerationService,
        textTranslationService: TextTranslationService,
        geminiService: LlmService? = null,
    ) : this(
        context = null,
        ocrService = ocrService,
        objectDetectionService = objectDetectionService,
        titleGenerationService = titleGenerationService,
        descriptionGenerationService = descriptionGenerationService,
        categoryClassificationService = categoryClassificationService,
        tagGenerationService = tagGenerationService,
        textTranslationService = textTranslationService,
        geminiService = geminiService,
    )

    suspend operator fun invoke(imageUri: String): DocumentDraft = coroutineScope {
        require(imageUri.isNotBlank()) { "An image URI is required" }

        // 01. If Gemini Vision API service is available, try extracting metadata directly from image.
        if (geminiService != null && geminiService.supportsVision && context != null) {
            val visionResult = runCatching {
                val uri = Uri.parse(imageUri)
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("Could not read image stream from URI")
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

                val prompt = LlmDocumentAnalyzer.buildVisionPrompt()
                val tokens = geminiService.generateVision(prompt, bytes, mimeType).toList()
                val raw = tokens.joinToString("")
                LlmDocumentAnalyzer.parseVision(raw)
            }.getOrNull()

            if (visionResult != null && (visionResult.title != null || visionResult.ocrText != null)) {
                return@coroutineScope DocumentDraft(
                    imageUri        = imageUri,
                    title           = visionResult.title ?: "Untitled Document",
                    description     = visionResult.description ?: "Document analyzed by Gemini Vision",
                    ocrText         = visionResult.ocrText ?: "",
                    category        = visionResult.category ?: DocumentCategory.OTHER,
                    tags            = visionResult.tags?.takeIf { it.isNotEmpty() } ?: listOf("document"),
                    detectedObjects = visionResult.detectedObjects?.takeIf { it.isNotEmpty() } ?: emptyList(),
                    warning         = null,
                )
            }
        }

        // 02. Fallback: 100% original on-device pipeline from UI branch (no extra LLM rewriting).
        runStandardPipeline(imageUri)
    }

    private suspend fun runStandardPipeline(imageUri: String): DocumentDraft = coroutineScope {
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
            imageUri        = imageUri,
            title           = titleDeferred.await(),
            description     = descriptionDeferred.await(),
            ocrText         = recognizedText,
            category        = categoryDeferred.await(),
            tags            = tagsDeferred.await(),
            detectedObjects = detectedObjects,
            warning         = null,
        )
    }
}
