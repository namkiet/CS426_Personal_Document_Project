package com.example.personaldocumentproject.domain.usecase

import com.example.personaldocumentproject.data.ml.llm.LlmDocumentAnalyzer
import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.LlmService
import com.example.personaldocumentproject.domain.service.MetadataService
import com.example.personaldocumentproject.domain.service.ObjectDetectionService
import com.example.personaldocumentproject.domain.service.OcrService
import kotlinx.coroutines.coroutineScope

class AnalyzeDocument(
    private val ocrService: OcrService,
    private val objectDetectionService: ObjectDetectionService,
    private val metadataService: MetadataService,
    /** Optional — when null the metadata service is used as the sole source. */
    private val llmService: LlmService? = null,
) {
    suspend operator fun invoke(imageUri: String): DocumentDraft = coroutineScope {
        require(imageUri.isNotBlank()) { "An image URI is required" }

        // 01. Extract raw text from the image using ML Kit OCR.
        val rawOcr = ocrService.recognizeText(imageUri)

        // 02. Detect and label objects found in the image.
        val detectedObjects = objectDetectionService.detectObjects(imageUri)

        // 03. If an LLM is available, fix garbled OCR output first, then extract metadata.
        //     If no LLM, fall through to MetadataService for all fields.
        val llmResult = llmService?.let { llm ->
            runCatching {
                // 03a. Fix OCR text (typos, broken words, artefacts).
                val fixedOcr = LlmDocumentAnalyzer.fixOcr(llm, rawOcr)

                // 03b. Extract structured metadata from the cleaned text.
                val prompt = LlmDocumentAnalyzer.buildPrompt(fixedOcr, detectedObjects)
                val metadata = LlmDocumentAnalyzer.call(llm, prompt)

                // Return both together so the fixed OCR can be stored.
                Pair(fixedOcr, metadata)
            }.getOrNull()
        }

        val ocrText        = llmResult?.first ?: rawOcr
        val metadata       = llmResult?.second

        val title          = metadata?.title          ?: metadataService.generateTitle(ocrText, detectedObjects)
        val description    = metadata?.description    ?: metadataService.generateDescription(ocrText, detectedObjects)
        val category       = metadata?.category       ?: metadataService.classifyCategory(ocrText, detectedObjects)
        val enrichedObjects = metadata?.detectedObjects?.takeIf { it.isNotEmpty() } ?: detectedObjects

        // 04. Tags always come from MetadataService (LLM is not asked for tags).
        val tags = metadataService.generateTags(ocrText, detectedObjects)

        // 05. Build and return the draft.
        DocumentDraft(
            imageUri        = imageUri,
            title           = title,
            description     = description,
            ocrText         = ocrText,
            category        = category,
            tags            = tags,
            detectedObjects = enrichedObjects,
        )
    }
}
