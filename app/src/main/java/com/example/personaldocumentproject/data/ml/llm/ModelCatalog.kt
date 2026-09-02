package com.example.personaldocumentproject.data.ml.llm

/**
 * A known on-device LLM that can be downloaded and used for inference.
 *
 * @param id            Stable identifier used as the SharedPreferences key and filename stem.
 * @param displayName   Human-readable name shown in the UI.
 * @param description   One-line capability summary shown in the UI.
 * @param fileName      Filename stored on device under the models directory.
 * @param downloadUrl   Direct URL to the `.litertlm` file.
 * @param sizeBytes     Approximate download size used for display.
 * @param ramRequiredMb Approximate RAM needed at inference time.
 */
data class ModelEntry(
    val id: String,
    val displayName: String,
    val description: String,
    val fileName: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val ramRequiredMb: Int,
)

/**
 * Central list of all supported LiteRT-LM models.
 * Add a new [ModelEntry] here to make a new model appear in the Model Manager screen.
 */
object ModelCatalog {
    val models: List<ModelEntry> = listOf(
        ModelEntry(
            id = "gemma-4-e2b",
            displayName = "Gemma 4 E2B",
            description = "Google's Gemma 4 (litert-community) – balanced quality and speed.",
            fileName = "gemma-4-e2b.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm",
            sizeBytes = 4_000_000_000L,   // ~4 GB
            ramRequiredMb = 4_096,
        ),
        ModelEntry(
            id = "qwen3-0.6b",
            displayName = "Qwen3 0.6B",
            description = "Qwen3 0.6B (litert-community) – ultra-lightweight for low-RAM devices.",
            fileName = "qwen3-0.6b.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/Qwen3-0.6B",
            sizeBytes = 600_000_000L,     // ~600 MB
            ramRequiredMb = 700,
        ),
    )

    fun findById(id: String): ModelEntry? = models.firstOrNull { it.id == id }
}
