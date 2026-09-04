package com.example.personaldocumentproject.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.personaldocumentproject.data.ml.llm.LlmDocumentAnalyzer
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.model.DocumentDraft
import com.example.personaldocumentproject.domain.service.LlmService
import com.example.personaldocumentproject.domain.service.MetadataService
import com.example.personaldocumentproject.domain.service.ObjectDetectionService
import com.example.personaldocumentproject.domain.service.OcrService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList

class AnalyzeDocument(
    private val context: Context? = null,
    private val ocrService: OcrService,
    private val objectDetectionService: ObjectDetectionService,
    private val metadataService: MetadataService,
    private val llmService: LlmService? = null,
) {
    // Secondary constructor for backward compatibility with unit tests (without context)
    constructor(
        ocrService: OcrService,
        objectDetectionService: ObjectDetectionService,
        metadataService: MetadataService,
        llmService: LlmService? = null,
    ) : this(null, ocrService, objectDetectionService, metadataService, llmService)

    suspend operator fun invoke(imageUri: String): DocumentDraft = coroutineScope {
        require(imageUri.isNotBlank()) { "An image URI is required" }

        val llm = llmService

        // 01. Detect if model supports vision and context is available
        if (llm != null && llm.supportsVision && context != null) {
            val visionResult = runCatching {
                val uri = Uri.parse(imageUri)
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("Could not read image stream from URI")
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

                val prompt = LlmDocumentAnalyzer.buildVisionPrompt()
                val tokens = llm.generateVision(prompt, bytes, mimeType).toList()
                val raw = tokens.joinToString("")
                LlmDocumentAnalyzer.parseVision(raw)
            }.getOrNull()

            if (visionResult != null && (visionResult.title != null || visionResult.ocrText != null)) {
                // Success: Vision model extracted schema info. Skip OCR and image labeling.
                return@coroutineScope DocumentDraft(
                    imageUri        = imageUri,
                    title           = visionResult.title          ?: "Untitled Document",
                    description     = visionResult.description    ?: "Document analyzed by Gemini Vision",
                    ocrText         = visionResult.ocrText        ?: "",
                    category        = visionResult.category       ?: DocumentCategory.OTHER,
                    tags            = visionResult.tags           ?.takeIf { it.isNotEmpty() } ?: listOf("document"),
                    detectedObjects = visionResult.detectedObjects?.takeIf { it.isNotEmpty() } ?: emptyList(),
                    warning         = null,
                )
            } else {
                // Vision call failed or unparseable: give warning and use only OCR and image labeling
                val warning = "⚠️ Vision model call failed. Fell back to OCR and image labeling."
                return@coroutineScope runStandardPipeline(imageUri, warning)
            }
        } else {
            // No vision support, no context, or No model active: use standard OCR and image labeling
            return@coroutineScope runStandardPipeline(imageUri, warning = null)
        }
    }

    private suspend fun runStandardPipeline(imageUri: String, warning: String?): DocumentDraft = coroutineScope {
        // 01. Extract raw text from the image using ML Kit OCR.
        val rawOcr = ocrService.recognizeText(imageUri)

        // 02. Detect and label objects found in the image.
        val detectedObjects = objectDetectionService.detectObjects(imageUri)

        // 03. If an LLM is available, fix garbled OCR output first, then extract metadata.
        val llm = llmService
        val llmResult = llm?.let { l ->
            runCatching {
                val fixedOcr = LlmDocumentAnalyzer.fixOcr(l, rawOcr)
                val prompt = LlmDocumentAnalyzer.buildPrompt(fixedOcr, detectedObjects)
                val metadata = LlmDocumentAnalyzer.call(l, prompt)
                Pair(fixedOcr, metadata)
            }.getOrNull()
        }

        val ocrText        = llmResult?.first ?: rawOcr
        val metadata       = llmResult?.second

        val title          = metadata?.title          ?: metadataService.generateTitle(ocrText, detectedObjects)
        val description    = metadata?.description    ?: metadataService.generateDescription(ocrText, detectedObjects)
        val category       = metadata?.category       ?: metadataService.classifyCategory(ocrText, detectedObjects)
        val enrichedObjects = metadata?.detectedObjects?.takeIf { it.isNotEmpty() } ?: detectedObjects
        val tags           = metadataService.generateTags(ocrText, detectedObjects)

        DocumentDraft(
            imageUri        = imageUri,
            title           = title,
            description     = description,
            ocrText         = ocrText,
            category        = category,
            tags            = tags,
            detectedObjects = enrichedObjects,
            warning         = warning,
        )
    }
}
