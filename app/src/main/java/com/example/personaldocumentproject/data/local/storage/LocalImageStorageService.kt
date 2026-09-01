package com.example.personaldocumentproject.data.local.storage

import android.content.Context
import android.net.Uri
import com.example.personaldocumentproject.domain.service.ImageStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class LocalImageStorageService(private val context: Context) : ImageStorageService {
    override suspend fun saveImageLocally(sourceUri: String): String = withContext(Dispatchers.IO) {
        val uri = Uri.parse(sourceUri)

        // If it's already a file within our app directory, just return it
        if (uri.scheme == "file" && uri.path?.contains(context.filesDir.path) == true) {
            return@withContext sourceUri
        }

        // Create a directory for our images if it doesn't exist
        val imagesDir = File(context.filesDir, "documents")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        // Generate a unique filename
        val fileName = "${UUID.randomUUID()}.jpg"
        val destinationFile = File(imagesDir, fileName)

        // Copy the stream
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destinationFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("Could not open input stream for URI: $sourceUri")

        Uri.fromFile(destinationFile).toString()
    }
}