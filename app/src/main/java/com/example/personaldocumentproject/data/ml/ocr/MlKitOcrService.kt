package com.example.personaldocumentproject.data.ml.ocr

import android.content.Context
import android.net.Uri
import com.example.personaldocumentproject.domain.service.OcrService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri

/** ML Kit integration point. Recognizes text from an image URI. */
class MlKitOcrService(private val context: Context) : OcrService {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognizeText(imageUri: String): String {
        return try {
            val uri = imageUri.toUri()
            val image = InputImage.fromFilePath(context, uri)

            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}