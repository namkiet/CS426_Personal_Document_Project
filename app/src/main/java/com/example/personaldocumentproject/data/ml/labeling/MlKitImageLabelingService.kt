package com.example.personaldocumentproject.data.ml.labeling

import android.content.Context
import android.net.Uri
import com.example.personaldocumentproject.domain.service.ImageLabelingService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await

class MlKitImageLabelingService(private val context: Context) : ImageLabelingService {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
    )

    override suspend fun generateTags(imageUri: String): List<String> {
        return try {
            val uri = Uri.parse(imageUri)
            val image = InputImage.fromFilePath(context, uri)

            val labels = labeler.process(image).await()
            labels.map { it.text }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}