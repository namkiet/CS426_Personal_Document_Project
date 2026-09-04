package com.example.personaldocumentproject.data.ml.llm

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import com.example.personaldocumentproject.domain.service.LlmService
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

private const val PREFS_NAME        = "model_manager"
private const val KEY_ACTIVE_MODEL  = "active_model_id"
private const val POLL_INTERVAL_MS  = 1_000L
private const val BROWSER_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

/**
 * Manages on-device model files and the user's active-model preference.
 *
 * @param geminiApiKey  Value from BuildConfig.GEMINI_API_KEY — injected by AppContainer
 *                      so the key never appears in source files.
 */
class ModelManagerRepository(
    private val context: Context,
    private val geminiApiKey: String,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // ── Model file helpers ────────────────────────────────────────────────────

    fun modelsDir(): File =
        (context.getExternalFilesDir("models") ?: context.filesDir.resolve("models"))
            .also { it.mkdirs() }

    fun modelFile(entry: ModelEntry): File = modelsDir().resolve(entry.fileName)

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
        } catch (_: Exception) { false }
    }

    fun isDownloaded(entry: ModelEntry): Boolean {
        if (entry.backend !is ModelBackend.OnDevice) return false
        val file = modelFile(entry)
        if (file.exists() && !isValidModelFile(file)) { file.delete(); return false }
        return file.exists()
    }

    // ── Active model selection ────────────────────────────────────────────────

    /**
     * Returns the active model id. Defaults to the first API model so the app
     * works out of the box without any download.
     */
    fun activeModelId(): String =
        prefs.getString(KEY_ACTIVE_MODEL, null)
            ?: ModelCatalog.models.firstOrNull { it.backend is ModelBackend.GeminiApi }?.id
            ?: ModelCatalog.models.first().id

    fun setActiveModelId(id: String) = prefs.edit { putString(KEY_ACTIVE_MODEL, id) }

    /**
     * Returns a ready-to-use [LlmService] for the active model.
     * - API models: always available (returns [GeminiApiLlmService]).
     * - On-device models: returns [LiteRtLmService] only if the file is present on disk.
     */
    fun activeServiceOrNull(): LlmService? {
        val entry = ModelCatalog.findById(activeModelId()) ?: return null
        return when (val backend = entry.backend) {
            is ModelBackend.GeminiApi -> {
                if (geminiApiKey.isBlank()) null
                else GeminiApiLlmService(apiKey = geminiApiKey, model = backend.geminiModel)
            }
            is ModelBackend.OnDevice -> {
                if (!isDownloaded(entry)) return null
                LiteRtLmService.forEntry(entry, modelFile(entry).absolutePath)
            }
            is ModelBackend.NoModel -> com.example.personaldocumentproject.di.NoModelLlmService
        }
    }

    // ── Download (on-device models only) ─────────────────────────────────────

    fun download(entry: ModelEntry): Flow<DownloadState> = flow {
        if (entry.backend !is ModelBackend.OnDevice) {
            emit(DownloadState.Done); return@flow
        }
        emit(DownloadState.Queued)
        if (isDownloaded(entry)) { emit(DownloadState.Done); return@flow }

        val destFile   = modelFile(entry)
        val candidates = resolveCandidates(entry)

        val viaDm = try {
            val request = DownloadManager.Request(Uri.parse(candidates.first()))
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
                if (cursor == null || !cursor.moveToFirst()) break

                val status     = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                val downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total      = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    .let { if (it > 0) it else entry.sizeBytes }
                cursor.close()

                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        if (isValidModelFile(destFile)) { emit(DownloadState.Done); success = true }
                        else { destFile.delete(); downloadManager.remove(downloadId) }
                        break
                    }
                    DownloadManager.STATUS_FAILED -> {
                        downloadManager.remove(downloadId); destFile.delete(); break
                    }
                    DownloadManager.STATUS_RUNNING,
                    DownloadManager.STATUS_PAUSED -> {
                        val pct = if (total > 0) (downloaded * 100L / total).toInt() else 0
                        emit(DownloadState.InProgress(pct, downloaded, total))
                    }
                    else -> { /* PENDING */ }
                }
                delay(POLL_INTERVAL_MS)
            }
            success
        } catch (_: Exception) { false }

        if (!viaDm) emitAll(downloadWithHttpURLConnection(entry, destFile, candidates))
    }

    private fun resolveCandidates(entry: ModelEntry): List<String> {
        val raw = entry.downloadUrl.trim().removeSuffix("/")
        if (!raw.contains("huggingface.co")) return listOf(raw)
        val baseRepo = if (raw.contains("/resolve/")) raw.substringBefore("/resolve/") else raw
        val repoName = baseRepo.substringAfterLast('/')
        return listOf(
            "$baseRepo/resolve/main/$repoName.litertlm",
            "$baseRepo/resolve/main/${entry.fileName}",
            "$baseRepo/resolve/main/gemma-4-E2B-it.litertlm",
            "$baseRepo/resolve/main/gemma-4-e2b.litertlm",
            "$baseRepo/resolve/main/Qwen3-0.6B.litertlm",
            "$baseRepo/resolve/main/qwen3-0.6b.litertlm",
            "$baseRepo/resolve/main/model.litertlm",
            raw,
        ).distinct()
    }

    private fun downloadWithHttpURLConnection(
        entry: ModelEntry,
        destFile: File,
        candidates: List<String>,
    ): Flow<DownloadState> = flow {
        emit(DownloadState.Queued)
        val tempFile = File(destFile.parentFile, "${destFile.name}.tmp")
        var lastError = "HTTP download failed"

        for (candidateUrl in candidates) {
            var connection: HttpURLConnection? = null
            try {
                var urlStr = candidateUrl
                var redirects = 0
                var code: Int

                while (true) {
                    connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 20_000; readTimeout = 30_000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", BROWSER_USER_AGENT)
                        setRequestProperty("Accept", "*/*")
                    }
                    code = connection.responseCode
                    if (code in listOf(301, 302, 307, 308)) {
                        val loc = connection.getHeaderField("Location")
                        connection.disconnect()
                        if (loc.isNullOrBlank() || ++redirects > 10) { code = -1; break }
                        urlStr = loc
                    } else break
                }

                if (code !in 200..299) {
                    lastError = "HTTP $code for $candidateUrl"
                    connection?.disconnect()
                    if (code == 404) continue else break
                }

                if (connection!!.contentType?.lowercase()?.contains("text/html") == true) {
                    lastError = "Server returned HTML instead of model file"
                    connection.disconnect(); continue
                }

                val total = connection.contentLengthLong.let { if (it > 0) it else entry.sizeBytes }
                connection.inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        val buf = ByteArray(64 * 1024)
                        var received = 0L; var n: Int; var lastEmit = 0L
                        while (input.read(buf).also { n = it } != -1) {
                            currentCoroutineContext().ensureActive()
                            output.write(buf, 0, n); received += n
                            val now = System.currentTimeMillis()
                            if (now - lastEmit >= 500) {
                                lastEmit = now
                                val pct = if (total > 0) (received * 100L / total).toInt() else 0
                                emit(DownloadState.InProgress(pct, received, total))
                            }
                        }
                    }
                }

                if (isValidModelFile(tempFile)) {
                    if (destFile.exists()) destFile.delete()
                    if (!tempFile.renameTo(destFile)) { tempFile.copyTo(destFile, overwrite = true); tempFile.delete() }
                    emit(DownloadState.Done); return@flow
                } else { tempFile.delete(); lastError = "Downloaded file is invalid" }

            } catch (e: CancellationException) { tempFile.delete(); throw e }
            catch (e: Exception) { tempFile.delete(); lastError = "Download failed: ${e.localizedMessage}" }
            finally { connection?.disconnect() }
        }
        emit(DownloadState.Failed(lastError))
    }.flowOn(Dispatchers.IO)

    fun deleteModel(entry: ModelEntry) {
        if (entry.backend !is ModelBackend.OnDevice) return
        modelFile(entry).delete()
        File(modelsDir(), "${entry.fileName}.tmp").takeIf { it.exists() }?.delete()
    }
}

sealed interface DownloadState {
    data object Queued : DownloadState
    data class InProgress(val percent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState
    data object Done : DownloadState
    data class Failed(val reason: String) : DownloadState
}
