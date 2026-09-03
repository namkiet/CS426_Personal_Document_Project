package com.example.personaldocumentproject.data.ml.llm

/**
 * LiteRT-LM service backed by the **Qwen 3 0.6B** model.
 *
 * ## Model download
 * Download the `.litertlm` bundle from Hugging Face or Kaggle Models:
 *   https://huggingface.co/Qwen/Qwen3-0.6B
 * Choose the LiteRT-LM (.litertlm) export and push it to device:
 *   ```
 *   adb push qwen3-0.6b.litertlm /sdcard/Android/data/com.example.personaldocumentproject/files/models/
 *   ```
 *
 * ## Expected model path
 * ```
 * val service = Qwen3_0_6BService(
 *     modelPath = context.getExternalFilesDir("models")
 *         ?.resolve("qwen3-0.6b.litertlm")
 *         ?.absolutePath ?: error("model not found")
 * )
 * ```
 *
 * ## Hardware requirements
 * - RAM: ~600 MB (int4 quantised) — suitable for low-RAM devices
 * - Very fast on CPU; preferred for latency-sensitive document tagging use cases
 */
class Qwen3_0_6BService(modelPath: String) : LiteRtLmService(
    modelPath = modelPath,
    modelName = "Qwen3-0.6B",
)
