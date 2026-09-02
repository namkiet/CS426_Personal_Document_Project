package com.example.personaldocumentproject.data.ml.objectdetection

import com.example.personaldocumentproject.domain.service.ObjectDetectionService

/** Object detection integration point. Replace this placeholder with an on-device model. */
class LocalObjectDetectionService : ObjectDetectionService {
    override suspend fun detectObjects(imageUri: String): List<String> = emptyList()
}
