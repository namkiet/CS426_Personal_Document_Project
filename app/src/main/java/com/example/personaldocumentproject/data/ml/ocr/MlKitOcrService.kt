package com.example.personaldocumentproject.data.ml.ocr

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.net.toUri
import com.example.personaldocumentproject.domain.service.OcrService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/** ML Kit integration point. Recognizes text from an image URI. */
class MlKitOcrService(private val context: Context) : OcrService {

    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    suspend fun prepare() {
        withContext(Dispatchers.Default) {
            runCatching {
                val dummyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                val dummyImage = InputImage.fromBitmap(dummyBitmap, 0)
                recognizer.process(dummyImage).await()
            }.onFailure { error ->
                if (error is CancellationException) throw error
                Log.w(TAG, "OCR warmup failed (non-fatal): ${error.message}")
            }
        }
    }

    override suspend fun recognizeText(imageUri: String): String {
        return try {
            val uri = imageUri.toUri()
            val image = InputImage.fromFilePath(context, uri)

            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Failed to recognize text from URI: $imageUri", e)
            ""
        }
    }

    private companion object {
        const val TAG = "MlKitOcrService"
    }
}
