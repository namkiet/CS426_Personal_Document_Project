package com.example.personaldocumentproject.data.ml.translation

import com.example.personaldocumentproject.domain.service.TextTranslationService
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/** Identifies OCR text and translates supported non-English languages on-device. */
class MlKitEnglishTranslationService : TextTranslationService {
    override suspend fun translateToEnglish(text: String): String {
        val sourceText = text.trim()
        if (sourceText.isBlank()) return sourceText

        return try {
            translate(sourceText)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            // Analysis remains usable when a model is unavailable or its download fails.
            sourceText
        }
    }

    private suspend fun translate(sourceText: String): String {
        val languageIdentifier = LanguageIdentification.getClient()
        val languageTag = try {
            languageIdentifier.identifyLanguage(sourceText).await()
        } finally {
            languageIdentifier.close()
        }

        if (languageTag == UNDETERMINED_LANGUAGE_TAG || languageTag == ENGLISH_LANGUAGE_TAG) {
            return sourceText
        }

        val sourceLanguage = TranslateLanguage.fromLanguageTag(languageTag) ?: return sourceText
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val translator = Translation.getClient(options)

        return try {
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
            translator.translate(sourceText).await().trim().ifBlank { sourceText }
        } finally {
            translator.close()
        }
    }

    private companion object {
        const val ENGLISH_LANGUAGE_TAG = "en"
        const val UNDETERMINED_LANGUAGE_TAG = "und"
    }
}
