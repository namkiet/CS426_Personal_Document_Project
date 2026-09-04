package com.example.personaldocumentproject.data.ml.llm

import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.flow.Flow

/**
 * Holds a long-running [Engine] + [Conversation] for persistent multi-turn sessions.
 *
 * Usage:
 * ```
 * val session = LiteRtLmSession(modelPath)
 * session.initialize()
 * session.sendMessage("Hello").collect { print(it) }
 * session.sendMessage("Follow-up").collect { print(it) }
 * session.close() // release engine resources when done
 * ```
 *
 * [close] should be called from a lifecycle-aware scope (e.g. ViewModel.onCleared).
 */
class LiteRtLmSession(private val modelPath: String) : AutoCloseable {
    private var engine: Engine? = null
    private var conversation: Conversation? = null

    suspend fun initialize() {
        LiteRtLmService.ensureNativeLibrariesLoaded()
        val config = EngineConfig(modelPath = modelPath)
        val eng = Engine(config)
        eng.initialize()
        engine = eng
        conversation = eng.createConversation()
    }

    /**
     * Send [message] to the persistent conversation and stream back token chunks.
     * [initialize] must be called before the first [sendMessage] call.
     */
    fun sendMessage(message: String): Flow<String> {
        val conv = checkNotNull(conversation) {
            "LiteRtLmSession: call initialize() before sendMessage()"
        }
        return conv.sendMessageStream(message)
    }

    override fun close() {
        conversation?.close()
        engine?.close()
        conversation = null
        engine = null
    }
}
