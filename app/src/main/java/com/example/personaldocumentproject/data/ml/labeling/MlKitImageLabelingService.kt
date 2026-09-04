package com.example.personaldocumentproject.data.ml.labeling

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.personaldocumentproject.domain.service.ObjectDetectionService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MlKitImageLabelingService(private val context: Context) : ObjectDetectionService {

    private val labeler by lazy {
        ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build()
        )
    }

    suspend fun prepare() {
        withContext(Dispatchers.Default) {
            runCatching {
                val dummyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                val dummyImage = InputImage.fromBitmap(dummyBitmap, 0)
                labeler.process(dummyImage).await()
            }.onFailure { error ->
                if (error is CancellationException) throw error
                Log.w(TAG, "Image labeling warmup failed (non-fatal): ${error.message}")
            }
        }
    }

    override suspend fun detectObjects(imageUri: String): List<String> {
        return try {
            val uri = Uri.parse(imageUri)
            val image = InputImage.fromFilePath(context, uri)

            val labels = labeler.process(image).await()
            labels.map { it.text }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Failed to detect objects from URI: $imageUri", e)
            emptyList()
        }
    }

    private companion object {
        const val TAG = "MlKitImageLabelingService"
    }
}
