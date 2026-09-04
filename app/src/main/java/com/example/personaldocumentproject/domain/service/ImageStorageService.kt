package com.example.personaldocumentproject.domain.service

interface ImageStorageService {
    /** Copies the image from the given URI to internal app storage and returns the new local URI. */
    suspend fun saveImageLocally(sourceUri: String): String

    /** Removes an image owned by the app. External content is left untouched. */
    suspend fun deleteImage(localUri: String) = Unit
}
