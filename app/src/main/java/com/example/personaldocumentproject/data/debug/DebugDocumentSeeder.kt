package com.example.personaldocumentproject.data.debug

import android.content.Context
import android.net.Uri
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DebugDocumentSeeder(
    private val context: Context,
    private val repository: DocumentRepository,
) {
    suspend fun seed() = withContext(Dispatchers.IO) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (preferences.getBoolean(KEY_SEEDED, false)) return@withContext

        val assetNames = context.assets.list(ASSET_DIRECTORY)
            ?.filter { it.endsWith(".jpg", ignoreCase = true) }
            ?.sorted()
            .orEmpty()
        val destinationDirectory = File(context.filesDir, "documents/debug-seed").apply { mkdirs() }
        val now = System.currentTimeMillis()

        assetNames.forEachIndexed { index, assetName ->
            val id = "debug-seed-${assetName.substringBeforeLast('.')}"
            val destination = File(destinationDirectory, assetName)
            if (!destination.exists()) {
                context.assets.open("$ASSET_DIRECTORY/$assetName").use { input ->
                    destination.outputStream().use(input::copyTo)
                }
            }

            if (repository.get(id) == null) {
                repository.save(
                    Document(
                        id = id,
                        imageUri = Uri.fromFile(destination).toString(),
                        title = "Debug image ${index + 1}",
                        description = "Image bundled for local debug testing.",
                        ocrText = "",
                        category = DocumentCategory.OTHER,
                        tags = listOf("debug", "seed"),
                        detectedObjects = emptyList(),
                        embedding = emptyList(),
                        createdAt = now - index,
                    )
                )
            }
        }
        preferences.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    private companion object {
        const val ASSET_DIRECTORY = "debug_seed_images"
        const val PREFERENCES_NAME = "debug_document_seed"
        const val KEY_SEEDED = "seeded"
    }
}
