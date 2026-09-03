package com.example.personaldocumentproject.domain.service

/** Translates recognized document text into English for downstream metadata and search. */
interface TextTranslationService {
    suspend fun translateToEnglish(text: String): String
}
