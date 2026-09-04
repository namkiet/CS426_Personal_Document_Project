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

    // ── Prompt ────────────────────────────────────────────────────────────────

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

    // ── Response parsing ──────────────────────────────────────────────────────

    data class ParsedResult(
        val title: String?,
        val description: String?,
        val category: DocumentCategory?,
        val detectedObjects: List<String>?,
    )

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
