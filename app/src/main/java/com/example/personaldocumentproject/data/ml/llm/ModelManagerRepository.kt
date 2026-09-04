package com.example.personaldocumentproject.data.ml.llm

import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException

private const val PREFS_NAME = "model_manager"
private const val KEY_ACTIVE_MODEL = "active_model_id"
private const val POLL_INTERVAL_MS = 1_000L
private const val BROWSER_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

/**
 * Manages on-device model files: tracks downloads via Android [DownloadManager],
 * checks which models are already present, and persists the user's active model choice.
 */
class ModelManagerRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // ── Model file helpers ────────────────────────────────────────────────────

    /** Directory where all `.litertlm` files are stored. */
    fun modelsDir(): File =
        (context.getExternalFilesDir("models") ?: context.filesDir.resolve("models"))
            .also { it.mkdirs() }

    fun modelFile(entry: ModelEntry): File = modelsDir().resolve(entry.fileName)

    /** Verifies that [file] exists, is non-empty, and is a binary model file (not HTML). */
    fun isValidModelFile(file: File): Boolean {
        if (!file.exists() || file.length() < 1024) return false
        return try {
            file.inputStream().use { input ->
                val buffer = ByteArray(16)
                val read = input.read(buffer)
                if (read < 4) return false
                val header = String(buffer, 0, read, Charsets.UTF_8).lowercase()
                !header.startsWith("<!doc") && !header.startsWith("<html")
            }
        } catch (_: Exception) {
            false
        }
    }

    fun isDownloaded(entry: ModelEntry): Boolean {
        val file = modelFile(entry)
        if (file.exists() && !isValidModelFile(file)) {
            // Delete corrupt or HTML landing page files automatically
            file.delete()
            return false
        }
        return file.exists()
    }

    // ── Active model selection ─────────────────────────────────────────────────

    /** Returns the currently selected model id, or the first catalog model as default. */
    fun activeModelId(): String =
        prefs.getString(KEY_ACTIVE_MODEL, null) ?: ModelCatalog.models.first().id

    fun setActiveModelId(id: String) {
        prefs.edit { putString(KEY_ACTIVE_MODEL, id) }
    }

    /** Returns a ready-to-use [LiteRtLmService] for the active model, or null if not downloaded. */
    fun activeServiceOrNull(): LiteRtLmService? {
        val entry = ModelCatalog.findById(activeModelId()) ?: return null
        if (!isDownloaded(entry)) return null
        val file = modelFile(entry)
        return LiteRtLmService.forEntry(entry, file.absolutePath)
    }

    // ── Download ───────────────────────────────────────────────────────────────

    /**
     * Enqueues a download for [entry] via [DownloadManager] (with browser User-Agent headers)
     * and falls back to direct HTTP connection if [DownloadManager] fails or downloads an HTML page.
     */
    fun download(entry: ModelEntry): Flow<DownloadState> = flow {
        emit(DownloadState.Queued)

        // Clean up invalid/corrupt downloads first
        if (isDownloaded(entry)) {
            emit(DownloadState.Done)
            return@flow
        }

        val destFile = modelFile(entry)
        val candidates = resolveCandidates(entry)
        val primaryUrl = candidates.first()

        val downloadedViaDm = try {
            val request = DownloadManager.Request(Uri.parse(primaryUrl))
                .setTitle("Downloading ${entry.displayName}")
                .setDescription("LiteRT-LM model")
                .addRequestHeader("User-Agent", BROWSER_USER_AGENT)
                .addRequestHeader("Accept", "*/*")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(destFile))

            val downloadId = downloadManager.enqueue(request)
            val query = DownloadManager.Query().setFilterById(downloadId)
            var success = false

            while (true) {
                val cursor = downloadManager.query(query)
                if (cursor == null || !cursor.moveToFirst()) {
                    break
                }

                val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val bytesCol = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalCol = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                val status = cursor.getInt(statusCol)
                val downloaded = cursor.getLong(bytesCol)
                val total = cursor.getLong(totalCol).takeIf { it > 0 } ?: entry.sizeBytes
                cursor.close()

                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        if (isValidModelFile(destFile)) {
                            emit(DownloadState.Done)
                            success = true
                        } else {
                            destFile.delete()
                            downloadManager.remove(downloadId)
                        }
                        break
                    }
                    DownloadManager.STATUS_FAILED -> {
                        downloadManager.remove(downloadId)
                        destFile.delete()
                        break
                    }
                    DownloadManager.STATUS_RUNNING,
                    DownloadManager.STATUS_PAUSED -> {
                        val progress = if (total > 0) (downloaded * 100L / total).toInt() else 0
                        emit(DownloadState.InProgress(progress, downloaded, total))
                    }
                    else -> { /* PENDING — still queued */ }
                }

                delay(POLL_INTERVAL_MS)
            }
            success
        } catch (_: Exception) {
            false
        }

        if (!downloadedViaDm) {
            emitAll(downloadWithHttpURLConnection(entry, destFile, candidates))
        }
    }

    /**
     * Resolves direct downloadable Hugging Face LFS URLs for a given [entry].
     * Transforms repo page URLs into direct /resolve/main/ candidate URLs.
     */
    private fun resolveCandidates(entry: ModelEntry): List<String> {
        val raw = entry.downloadUrl.trim().removeSuffix("/")
        if (raw.contains("huggingface.co")) {
            val baseRepo = if (raw.contains("/resolve/")) {
                raw.substringBefore("/resolve/")
            } else {
                raw
            }
            val repoName = baseRepo.substringAfterLast('/')
            return listOf(
                "$baseRepo/resolve/main/$repoName.litertlm",
                "$baseRepo/resolve/main/${entry.fileName}",
                "$baseRepo/resolve/main/gemma-4-E2B-it.litertlm",
                "$baseRepo/resolve/main/gemma-4-e2b.litertlm",
                "$baseRepo/resolve/main/Qwen3-0.6B.litertlm",
                "$baseRepo/resolve/main/qwen3-0.6b.litertlm",
                "$baseRepo/resolve/main/model.litertlm",
                "$baseRepo/resolve/main/model.bin",
                raw
            ).distinct()
        }
        return listOf(raw)
    }

    /** Direct HTTP stream download fallback to handle 301/302 redirects and custom User-Agent headers. */
    private fun downloadWithHttpURLConnection(
        entry: ModelEntry,
        destFile: File,
        candidates: List<String> = resolveCandidates(entry)
    ): Flow<DownloadState> = flow {
        emit(DownloadState.Queued)
        val tempFile = File(destFile.parentFile, "${destFile.name}.tmp")
        var lastErrorMessage = "HTTP download failed"

        for (candidateUrl in candidates) {
            var connection: HttpURLConnection? = null
            try {
                var urlStr = candidateUrl
                var redirectCount = 0
                val maxRedirects = 10
                var responseCode: Int

                while (true) {
                    val url = URL(urlStr)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 20_000
                        readTimeout = 30_000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", BROWSER_USER_AGENT)
                        setRequestProperty("Accept", "*/*")
                    }
                    responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == 307 || responseCode == 308
                    ) {
                        val newUrl = connection.getHeaderField("Location")
                        connection.disconnect()
                        if (newUrl.isNullOrBlank() || ++redirectCount > maxRedirects) {
                            responseCode = -1
                            lastErrorMessage = "HTTP error: Too many redirects"
                            break
                        }
                        urlStr = newUrl
                    } else {
                        break
                    }
                }

                if (responseCode !in 200..299) {
                    if (responseCode != -1) {
                        lastErrorMessage = when (responseCode) {
                            403 -> "HTTP 403 Forbidden: Access denied. Server blocks request or requires authorization."
                            404 -> "HTTP 404 Not Found: File not found at $candidateUrl"
                            else -> "HTTP download failed with code $responseCode"
                        }
                    }
                    connection.disconnect()
                    if (responseCode == 404) continue else break
                }

                val contentType = connection.contentType?.lowercase() ?: ""
                if (contentType.contains("text/html")) {
                    lastErrorMessage = "Server returned HTML web page instead of binary model file"
                    connection.disconnect()
                    continue
                }

                val totalBytes = connection.contentLengthLong.let { if (it > 0) it else entry.sizeBytes }

                connection.inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var bytesDownloaded = 0L
                        var bytesRead: Int
                        var lastEmitTime = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            currentCoroutineContext().ensureActive()
                            output.write(buffer, 0, bytesRead)
                            bytesDownloaded += bytesRead

                            val now = System.currentTimeMillis()
                            if (now - lastEmitTime >= 500) {
                                lastEmitTime = now
                                val percent = if (totalBytes > 0) (bytesDownloaded * 100L / totalBytes).toInt() else 0
                                emit(DownloadState.InProgress(percent, bytesDownloaded, totalBytes))
                            }
                        }
                    }
                }

                if (isValidModelFile(tempFile)) {
                    if (destFile.exists()) destFile.delete()
                    if (tempFile.renameTo(destFile)) {
                        emit(DownloadState.Done)
                    } else {
                        tempFile.copyTo(destFile, overwrite = true)
                        tempFile.delete()
                        emit(DownloadState.Done)
                    }
                    return@flow
                } else {
                    tempFile.delete()
                    lastErrorMessage = "Downloaded file is invalid or HTML web page"
                }

            } catch (e: CancellationException) {
                tempFile.delete()
                throw e
            } catch (e: Exception) {
                tempFile.delete()
                lastErrorMessage = "Download failed: ${e.localizedMessage ?: e.message}"
            } finally {
                connection?.disconnect()
            }
        }

        emit(DownloadState.Failed(lastErrorMessage))
    }.flowOn(Dispatchers.IO)

    /** Cancel and remove an in-progress or queued download for [entry]. */
    fun deleteModel(entry: ModelEntry) {
        modelFile(entry).delete()
        val tempFile = File(modelsDir(), "${entry.fileName}.tmp")
        if (tempFile.exists()) tempFile.delete()
    }
}

/** Progress snapshot emitted while a model is being downloaded. */
sealed interface DownloadState {
    data object Queued : DownloadState
    data class InProgress(val percent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState
    data object Done : DownloadState
    data class Failed(val reason: String) : DownloadState
}
