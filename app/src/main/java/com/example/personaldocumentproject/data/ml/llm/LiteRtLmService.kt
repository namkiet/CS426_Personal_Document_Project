package com.example.personaldocumentproject.data.ml.llm

import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.example.personaldocumentproject.domain.service.LlmService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Base LiteRT-LM implementation shared by concrete model classes.
 *
 * @param modelPath Absolute path to the `.litertlm` model file on device storage.
 *                  Typically placed under the app's `filesDir` or on external storage.
 *                  For testing, push the file via adb:
 *                      adb push model.litertlm /sdcard/Documents/
 *                  and pass the path here.
 */
abstract class LiteRtLmService(
    private val modelPath: String,
    override val modelName: String,
) : LlmService {

    /**
     * Run a single-turn generation: no conversation state is maintained across calls.
     * Opens and closes an [Engine] for each call so that each call is independent.
     */
    override fun generate(prompt: String): Flow<String> = flow {
        val config = EngineConfig(modelPath = modelPath)
        Engine(config).use { engine ->
            engine.initialize()
            engine.createConversation().use { conversation ->
                emitAll(conversation.sendMessageStream(prompt))
            }
        }
    }

    /**
     * Run a multi-turn chat session.
     * This implementation re-uses a *per-call* conversation, meaning each [chat] call
     * creates a fresh engine + conversation. To maintain actual conversational context,
     * the caller should include prior turns in the [prompt] string (e.g. formatted as
     * "<user>\nhi\n<model>\nhello\n<user>\nnext question").
     *
     * If you need a long-running persistent conversation across multiple UI interactions,
     * use [LiteRtLmSession] instead, which keeps the Engine alive for the session lifetime.
     */
    override fun chat(prompt: String): Flow<String> = generate(prompt)

    companion object {
        init {
            ensureNativeLibrariesLoaded()
        }

        fun ensureNativeLibrariesLoaded() {
            try {
                System.loadLibrary("litertlm_jni")
            } catch (_: Throwable) {
                try {
                    System.loadLibrary("litertlm_native")
                } catch (_: Throwable) {}
            }
        }

        /** Create the correct concrete service for a [ModelEntry] at the given [modelPath]. */
        fun forEntry(entry: ModelEntry, modelPath: String): LiteRtLmService = when (entry.id) {
            "gemma-4-e2b" -> Gemma4E2bService(modelPath)
            "qwen3-0.6b"  -> Qwen3_0_6BService(modelPath)
            else           -> GenericLiteRtLmService(modelPath, entry.displayName)
        }
    }
}

/**
 * Extension function wrapping [Conversation.sendMessageAsync] with a custom [callbackFlow].
 * Replaces LiteRT-LM's internal flow wrapper to avoid binary incompatibility with `SendChannel.close$default`.
 */
fun Conversation.sendMessageStream(prompt: String): Flow<String> = callbackFlow {
    val callback = object : MessageCallback {
        override fun onMessage(message: Message) {
            trySend(message.toString())
        }

        override fun onDone() {
            close()
        }

        override fun onError(throwable: Throwable) {
            close(throwable)
        }
    }

    sendMessageAsync(prompt, callback)

    awaitClose { /* No-op cleanup */ }
}

/** Fallback for catalog entries that don't have a dedicated class yet. */
private class GenericLiteRtLmService(modelPath: String, name: String) :
    LiteRtLmService(modelPath, name)
