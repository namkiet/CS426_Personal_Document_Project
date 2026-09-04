package com.example.personaldocumentproject.data.ml.llm

/**
 * Discriminates between a model file downloaded to device storage,
 * a cloud model accessed via an API key, or no model (disabled).
 */
sealed interface ModelBackend {
    /** `.litertlm` file stored on device. Download required before use. */
    data object OnDevice : ModelBackend

    /**
     * Cloud model accessed via REST API.
     * [geminiModel] is the Gemini model id (e.g. "gemini-3.1-flash").
     * No download required; availability depends on network + API key.
     */
    data class GeminiApi(val geminiModel: String) : ModelBackend

    /** No AI model active; uses OCR and metadata heuristics only. */
    data object NoModel : ModelBackend
}

/**
 * A known LLM that can be selected in the Model Manager.
 *
 * @param id            Stable identifier stored in SharedPreferences.
 * @param displayName   Human-readable name shown in the UI.
 * @param description   One-line capability summary.
 * @param backend       How the model is accessed ([ModelBackend.OnDevice], [ModelBackend.GeminiApi], or [ModelBackend.NoModel]).
 *
 * On-device only:
 * @param fileName      Filename under the models directory. Empty for API/NoModel models.
 * @param downloadUrl   URL to the `.litertlm` file. Empty for API/NoModel models.
 * @param sizeBytes     Approximate download size. 0 for API/NoModel models.
 * @param ramRequiredMb Approximate RAM at inference time. 0 for API/NoModel models.
 */
data class ModelEntry(
    val id: String,
    val displayName: String,
    val description: String,
    val backend: ModelBackend,
    // On-device fields (unused for API/NoModel models)
    val fileName: String = "",
    val downloadUrl: String = "",
    val sizeBytes: Long = 0L,
    val ramRequiredMb: Int = 0,
)

/**
 * Central list of all supported models.
 * Add a new [ModelEntry] here to make it appear in the Model Manager.
 */
object ModelCatalog {
    val models: List<ModelEntry> = listOf(

        // ── No Model option ───────────────────────────────────────────────
        ModelEntry(
            id          = "no_model",
            displayName = "No Model (OCR & Heuristics Only)",
            description = "Disable AI features; use ML Kit OCR and metadata heuristics.",
            backend     = ModelBackend.NoModel,
        ),

        // ── Cloud / API models (no download required) ─────────────────────
        ModelEntry(
            id          = "gemini-3.1-flash",
            displayName = "Gemini 3.1 Flash",
            description = "Google's fastest multimodal model — best for everyday tasks.",
            backend     = ModelBackend.GeminiApi("gemini-3.1-flash"),
        ),
        ModelEntry(
            id          = "gemini-3.1-flash-lite",
            displayName = "Gemini 3.1 Flash Lite",
            description = "Ultra-low latency, cost-efficient variant of Flash.",
            backend     = ModelBackend.GeminiApi("gemini-3.1-flash-lite"),
        ),

        // ── On-device models (download required) ──────────────────────────
        ModelEntry(
            id           = "gemma-4-e2b",
            displayName  = "Gemma 4 E2B (on-device)",
            description  = "Google's Gemma 4 — balanced quality, runs fully offline.",
            backend      = ModelBackend.OnDevice,
            fileName     = "gemma-4-e2b.litertlm",
            downloadUrl  = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm",
            sizeBytes    = 4_000_000_000L,
            ramRequiredMb = 4_096,
        ),
        ModelEntry(
            id           = "qwen3-0.6b",
            displayName  = "Qwen3 0.6B (on-device)",
            description  = "Ultra-lightweight, runs on low-RAM devices fully offline.",
            backend      = ModelBackend.OnDevice,
            fileName     = "qwen3-0.6b.litertlm",
            downloadUrl  = "https://huggingface.co/litert-community/Qwen3-0.6B",
            sizeBytes    = 600_000_000L,
            ramRequiredMb = 700,
        ),
    )

    fun findById(id: String): ModelEntry? = models.firstOrNull { it.id == id }
}
