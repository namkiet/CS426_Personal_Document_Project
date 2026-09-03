package com.example.personaldocumentproject.domain.service

/** Translates recognized document text into English for description generation. */
interface TextTranslationService {
    suspend fun translateToEnglish(text: String): String
}
