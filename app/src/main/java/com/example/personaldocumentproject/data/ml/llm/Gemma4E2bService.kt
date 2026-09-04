package com.example.personaldocumentproject.data.ml.llm

/**
 * LiteRT-LM service backed by the **Gemma 4 E2B** model.
 *
 * ## Model download
 * Download the `.litertlm` bundle from Google AI Edge Hub or Kaggle Models:
 *   https://www.kaggle.com/models/google/gemma/
 * Choose the "gemma-4-e2b" variant in LiteRT-LM (.litertlm) format and push it to device:
 *   ```
 *   adb push gemma-4-e2b.litertlm /sdcard/Android/data/com.example.personaldocumentproject/files/models/
 *   ```
 *
 * ## Expected model path
 * Pass the absolute path to the `.litertlm` file. In production, resolve it from
 * `context.getExternalFilesDir("models")` or `context.filesDir`:
 * ```
 * val service = Gemma4E2bService(
 *     modelPath = context.getExternalFilesDir("models")
 *         ?.resolve("gemma-4-e2b.litertlm")
 *         ?.absolutePath ?: error("model not found")
 * )
 * ```
 *
 * ## Hardware requirements
 * - RAM: ~4 GB (int4 quantised)
 * - Works on CPU; GPU acceleration available where supported by LiteRT-LM backend
 */
class Gemma4E2bService(modelPath: String) : LiteRtLmService(
    modelPath = modelPath,
    modelName = "Gemma-4-E2B",
)
