package com.example.personaldocumentproject.data.ml.llm

import com.example.personaldocumentproject.domain.service.LlmService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * [LlmService] backed by the Gemini API (AI Studio / Google Gen AI).
 *
 * Uses streaming server-sent events (generateContentStream) so tokens arrive
 * incrementally, consistent with the on-device LiteRT-LM interface.
 *
 * The API key is injected at construction time from BuildConfig — it is never
 * hard-coded in source files.
 *
 * @param apiKey  Value of BuildConfig.GEMINI_API_KEY.
 * @param model   Gemini model id, e.g. "gemini-3.1-flash" or "gemini-3.1-flash-lite".
 */
class GeminiApiLlmService(
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL,
) : LlmService {

    override val modelName: String = "Gemini ($model)"
    override val supportsVision: Boolean = true

    override suspend fun generateVision(prompt: String, imageBytes: ByteArray, mimeType: String): Flow<String> = flow {
        val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        val url = URL(
            "https://generativelanguage.googleapis.com/v1beta/models/$model" +
            ":streamGenerateContent?alt=sse&key=$apiKey"
        )

        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", mimeType)
                                put("data", base64Image)
                            })
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("maxOutputTokens", 2048)
            })
        }.toString()

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 20_000
            readTimeout = 60_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "text/event-stream")
            outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        }

        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
            conn.disconnect()
            throw GeminiApiException("Gemini API error $code: $err")
        }

        BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { reader ->
            for (line in reader.lineSequence()) {
                if (!line.startsWith("data:")) continue
                val json = line.removePrefix("data:").trim()
                if (json == "[DONE]" || json.isBlank()) continue
                val token = extractToken(json) ?: continue
                if (token.isNotEmpty()) emit(token)
            }
        }
        conn.disconnect()
    }.flowOn(Dispatchers.IO)

    // ── generate — single-turn, streaming ────────────────────────────────────

    override fun generate(prompt: String): Flow<String> = flow {
        val url = URL(
            "https://generativelanguage.googleapis.com/v1beta/models/$model" +
            ":streamGenerateContent?alt=sse&key=$apiKey"
        )

        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("maxOutputTokens", 2048)
            })
        }.toString()

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 20_000
            readTimeout = 60_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "text/event-stream")
            outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        }

        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
            conn.disconnect()
            throw GeminiApiException("Gemini API error $code: $err")
        }

        // Parse SSE stream: each event is "data: {...json...}\n\n"
        BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { reader ->
            for (line in reader.lineSequence()) {
                if (!line.startsWith("data:")) continue
                val json = line.removePrefix("data:").trim()
                if (json == "[DONE]" || json.isBlank()) continue
                val token = extractToken(json) ?: continue
                if (token.isNotEmpty()) emit(token)
            }
        }
        conn.disconnect()
    }.flowOn(Dispatchers.IO)

    // ── chat — same as generate for single-turn use case ─────────────────────

    override fun chat(prompt: String): Flow<String> = generate(prompt)

    // ── JSON parsing ──────────────────────────────────────────────────────────

    private fun extractToken(json: String): String? = runCatching {
        val root = JSONObject(json)
        root
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }.getOrNull()

    companion object {
        const val DEFAULT_MODEL = "gemini-3.1-flash-lite"

    }
}

class GeminiApiException(message: String) : Exception(message)
