package com.example.personaldocumentproject.presentation.modelmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.data.ml.llm.DownloadState
import com.example.personaldocumentproject.data.ml.llm.ModelCatalog
import com.example.personaldocumentproject.data.ml.llm.ModelEntry
import com.example.personaldocumentproject.data.ml.llm.ModelManagerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ModelManagerViewModel(
    private val repository: ModelManagerRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow(ModelManagerUiState())
    val uiState = mutableState.asStateFlow()

    /** Active download jobs keyed by model id — allows cancellation. */
    private val downloadJobs = mutableMapOf<String, Job>()

    init {
        refresh()
    }

    /** Re-scan disk and rebuild row state from scratch. */
    fun refresh() {
        val activeId = repository.activeModelId()
        mutableState.value = ModelManagerUiState(
            isLoading = false,
            models = ModelCatalog.models.map { entry ->
                ModelRowState(
                    entry = entry,
                    isDownloaded = repository.isDownloaded(entry),
                    isActive = entry.id == activeId,
                    isDownloading = downloadJobs[entry.id]?.isActive == true,
                    downloadPercent = if (downloadJobs[entry.id]?.isActive == true) 0 else null,
                )
            },
        )
    }

    /** Start downloading [entry] if not already in progress. */
    fun download(entry: ModelEntry) {
        if (downloadJobs[entry.id]?.isActive == true) return
        updateRow(entry.id) { it.copy(isDownloading = true, downloadPercent = 0, errorMessage = null) }

        val job = viewModelScope.launch {
            repository.download(entry).collect { state ->
                when (state) {
                    is DownloadState.Queued -> updateRow(entry.id) {
                        it.copy(isDownloading = true, downloadPercent = 0)
                    }
                    is DownloadState.InProgress -> updateRow(entry.id) {
                        it.copy(isDownloading = true, downloadPercent = state.percent)
                    }
                    is DownloadState.Done -> {
                        updateRow(entry.id) {
                            it.copy(isDownloading = false, downloadPercent = null, isDownloaded = true)
                        }
                        // Auto-activate the first successfully downloaded model
                        if (!mutableState.value.models.any { it.isActive && it.isDownloaded }) {
                            selectModel(entry)
                        }
                    }
                    is DownloadState.Failed -> updateRow(entry.id) {
                        it.copy(isDownloading = false, downloadPercent = null, errorMessage = state.reason)
                    }
                }
            }
        }
        downloadJobs[entry.id] = job
    }

    /** Cancel an in-progress download for [entry]. */
    fun cancelDownload(entry: ModelEntry) {
        downloadJobs[entry.id]?.cancel()
        downloadJobs.remove(entry.id)
        repository.deleteModel(entry)
        updateRow(entry.id) { it.copy(isDownloading = false, downloadPercent = null) }
    }

    /** Delete a downloaded model file from disk. Cannot delete the active model. */
    fun deleteModel(entry: ModelEntry) {
        val row = mutableState.value.models.find { it.entry.id == entry.id } ?: return
        if (row.isActive) return  // never delete what's in use
        repository.deleteModel(entry)
        updateRow(entry.id) { it.copy(isDownloaded = false) }
    }

    /** Set [entry] as the active model used for AI enhancement. */
    fun selectModel(entry: ModelEntry) {
        repository.setActiveModelId(entry.id)
        mutableState.update { state ->
            state.copy(models = state.models.map { row ->
                row.copy(isActive = row.entry.id == entry.id)
            })
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun updateRow(id: String, block: (ModelRowState) -> ModelRowState) {
        mutableState.update { state ->
            state.copy(models = state.models.map { if (it.entry.id == id) block(it) else it })
        }
    }
}
