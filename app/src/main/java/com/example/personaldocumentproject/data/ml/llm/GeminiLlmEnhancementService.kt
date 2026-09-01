package com.example.personaldocumentproject.data.ml.llm

import com.example.personaldocumentproject.domain.service.LlmEnhancementService
import com.google.firebase.Firebase
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.vertexAI

class GeminiLlmEnhancementService : LlmEnhancementService {
    // Note: requires setting up a Firebase project and google-services.json for production
    private val model: GenerativeModel? by lazy {
        try {
            Firebase.vertexAI.generativeModel("gemini-1.5-flash")
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun enhanceDocumentData(
        ocrText: String,
        imageLabels: List<String>,
        languageHint: String?
    ): String {
        if (ocrText.isBlank()) return ocrText

        val generativeModel = model ?: return ocrText

        val languageInstruction = if (!languageHint.isNullOrBlank()) {
            "Please format the result in $languageHint language. If the OCR text appears to be another language, attempt to fix typos based on $languageHint context."
        } else {
            "Please infer the correct language from the OCR text and fix any typos."
        }

        val prompt = """
            You are a helpful assistant correcting OCR mistakes from a scanned document.
            I have run an image through an ML Kit OCR scanner and an Image Labeling model.

            Here are the image labels detected in the image: ${imageLabels.joinToString(", ")}

            Here is the raw OCR text extracted:
            ---
            $ocrText
            ---

            $languageInstruction

            Please combine the visual context from the labels and the raw OCR text to produce a clean, corrected, and well-formatted version of the text. Do not add conversational filler, only return the corrected text.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: ocrText
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to original text if API fails (e.g. no internet or Firebase config missing)
            ocrText
        }
    }
}