package com.example.personaldocumentproject.data.ml.llm

import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.service.LlmService
import kotlinx.coroutines.flow.toList

/**
 * Builds a structured prompt for the LLM and parses its JSON response back into
 * typed fields used by [DocumentDraft].
 *
 * The LLM is asked to return ONLY a JSON object — no markdown, no prose — so that
 * we can reliably extract fields even from streaming, concatenated token output.
 *
 * Fields returned:
 *   - title          : short human-readable title (≤ 60 chars)
 *   - description    : one-sentence summary of the document
 *   - category       : one of NOTE | RECEIPT | IDENTITY | STUDY | WORK | OTHER
 *   - detectedObjects: JSON array of object names visible in the image
 *
 * Tags are intentionally omitted from the prompt and left to the existing metadata service.
 */
object LlmDocumentAnalyzer {

    // ── OCR fix prompt ────────────────────────────────────────────────────────

    /**
     * Builds a prompt that asks the LLM to fix garbled OCR output.
     * The model corrects typos, broken words, and formatting artefacts while
     * preserving the original meaning. It returns ONLY the cleaned plain text.
     */
    fun buildOcrFixPrompt(rawOcr: String): String = """
You are an OCR correction assistant. The text below was extracted by an OCR engine and may contain
garbled characters, broken words, missing spaces, or formatting artefacts.

Rewrite it as clean, readable plain text. Preserve all original meaning, numbers, and proper nouns.
Do NOT add any explanation, markdown, or extra commentary — output ONLY the corrected text.

--- RAW OCR ---
${rawOcr.take(4000)}
    """.trimIndent()

    /**
     * Sends [rawOcr] to the LLM for cleanup and returns the corrected text.
     * Returns the original [rawOcr] unchanged if the LLM call fails or returns blank.
     */
    suspend fun fixOcr(llm: LlmService, rawOcr: String): String {
        if (rawOcr.isBlank()) return rawOcr
        val tokens = llm.generate(buildOcrFixPrompt(rawOcr)).toList()
        val fixed = tokens.joinToString("").trim()
        return fixed.ifBlank { rawOcr }
    }

    // ── Metadata prompt ───────────────────────────────────────────────────────

    fun buildPrompt(ocrText: String, detectedObjects: List<String>): String {
        val objectList = if (detectedObjects.isEmpty()) "none detected"
                         else detectedObjects.joinToString(", ")

        return """
You are a document analysis assistant. Analyze the document below and respond with ONLY a valid JSON object — no markdown, no code fences, no explanation.

The JSON must have exactly these keys:
  "title"          – a short human-readable title for the document (max 60 characters)
  "description"    – one concise sentence summarising what the document contains
  "category"       – one of: NOTE, RECEIPT, IDENTITY, STUDY, WORK, OTHER
  "detectedObjects"– a JSON array of strings listing physical objects visible in the image

--- OCR TEXT ---
${ocrText.take(3000).ifBlank { "(no text detected)" }}

--- DETECTED OBJECTS ---
$objectList

Respond with ONLY the JSON object, nothing else.
        """.trimIndent()
    }

    // ── Vision prompt ─────────────────────────────────────────────────────────

    fun buildVisionPrompt(): String = """
You are an advanced multimodal document analysis assistant. Analyze the document image and respond with ONLY a valid JSON object — no markdown, no code fences, no explanation.

The JSON must have exactly these keys:
  "title"          – a short human-readable title for the document (max 60 characters)
  "description"    – one concise sentence summarising what the document contains
  "ocrText"        – the full plain text read or extracted from the document image
  "category"       – one of: NOTE, RECEIPT, IDENTITY, STUDY, WORK, OTHER
  "tags"           – a JSON array of keyword strings for the document
  "detectedObjects"– a JSON array of strings listing physical objects or elements visible in the image

Respond with ONLY the JSON object, nothing else.
    """.trimIndent()

    // ── Response parsing ──────────────────────────────────────────────────────

    data class ParsedResult(
        val title: String?,
        val description: String?,
        val category: DocumentCategory?,
        val detectedObjects: List<String>?,
    )

    data class VisionParsedResult(
        val title: String?,
        val description: String?,
        val ocrText: String?,
        val category: DocumentCategory?,
        val tags: List<String>?,
        val detectedObjects: List<String>?,
    )

    fun parseVision(raw: String): VisionParsedResult {
        val jsonBlock = extractJsonBlock(raw)
            ?: return VisionParsedResult(null, null, null, null, null, null)

        return VisionParsedResult(
            title           = extractString(jsonBlock, "title"),
            description     = extractString(jsonBlock, "description"),
            ocrText         = extractString(jsonBlock, "ocrText"),
            category        = extractCategory(jsonBlock),
            tags            = extractStringArray(jsonBlock, "tags"),
            detectedObjects = extractStringArray(jsonBlock, "detectedObjects"),
        )
    }

    /**
     * Collect all streamed tokens into one string and extract the first valid JSON object.
     * Returns [ParsedResult] with null fields for anything that cannot be parsed.
     */
    suspend fun call(llm: LlmService, prompt: String): ParsedResult {
        val tokens = llm.generate(prompt).toList()
        val raw = tokens.joinToString("")
        return parse(raw)
    }

    fun parse(raw: String): ParsedResult {
        // Find the first { ... } block in the response (LLM may emit preamble text)
        val jsonBlock = extractJsonBlock(raw)
            ?: return ParsedResult(null, null, null, null)

        return ParsedResult(
            title           = extractString(jsonBlock, "title"),
            description     = extractString(jsonBlock, "description"),
            category        = extractCategory(jsonBlock),
            detectedObjects = extractStringArray(jsonBlock, "detectedObjects"),
        )
    }

    // ── Parsing helpers ───────────────────────────────────────────────────────

    /** Extracts the first top-level {...} block from [text]. */
    private fun extractJsonBlock(text: String): String? {
        val start = text.indexOf('{')
        if (start < 0) return null
        var depth = 0
        for (i in start until text.length) {
            when (text[i]) {
                '{' -> depth++
                '}' -> { depth--; if (depth == 0) return text.substring(start, i + 1) }
            }
        }
        return null
    }

    /** Extracts a string value for [key] from a JSON block using simple regex. */
    private fun extractString(json: String, key: String): String? {
        // Matches: "key" : "value"  (handles escaped quotes inside value via non-greedy)
        val pattern = Regex("\"$key\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
        return pattern.find(json)?.groupValues?.getOrNull(1)
            ?.replace("\\n", "\n")
            ?.replace("\\\"", "\"")
            ?.replace("\\\\", "\\")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    /** Maps the "category" field to a [DocumentCategory] enum value. */
    private fun extractCategory(json: String): DocumentCategory? {
        val raw = extractString(json, "category")?.uppercase() ?: return null
        return DocumentCategory.entries.firstOrNull { it.name == raw }
    }

    /**
     * Extracts a JSON string array value for [key].
     * Handles both `["a","b"]` and `["a", "b"]` formatting.
     */
    private fun extractStringArray(json: String, key: String): List<String>? {
        // Find the array bracket block for this key
        val keyIdx = json.indexOf("\"$key\"")
        if (keyIdx < 0) return null
        val colonIdx = json.indexOf(':', keyIdx)
        if (colonIdx < 0) return null
        val arrayStart = json.indexOf('[', colonIdx)
        if (arrayStart < 0) return null
        val arrayEnd = json.indexOf(']', arrayStart)
        if (arrayEnd < 0) return null

        val arrayContent = json.substring(arrayStart + 1, arrayEnd)
        val itemPattern = Regex("\"((?:[^\"\\\\]|\\\\.)*)\"")
        return itemPattern.findAll(arrayContent)
            .map { it.groupValues[1].trim() }
            .filter { it.isNotBlank() }
            .toList()
            .takeIf { it.isNotEmpty() }
    }
}
