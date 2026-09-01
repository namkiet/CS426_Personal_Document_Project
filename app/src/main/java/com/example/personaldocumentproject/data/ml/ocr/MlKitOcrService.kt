package com.example.personaldocumentproject.data.ml.ocr

import com.example.personaldocumentproject.domain.service.OcrService

/** ML Kit integration point. The scaffold returns no text until the runtime is configured. */
class MlKitOcrService : OcrService {
    override suspend fun recognizeText(imageUri: String): String = ""
}
