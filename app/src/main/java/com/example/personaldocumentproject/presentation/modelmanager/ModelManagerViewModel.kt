package com.example.personaldocumentproject.presentation.modelmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.data.ml.llm.DownloadState
import com.example.personaldocumentproject.data.ml.llm.ModelBackend
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

    private val downloadJobs = mutableMapOf<String, Job>()

    init { refresh() }

    fun refresh() {
        val activeId = repository.activeModelId()
        mutableState.value = ModelManagerUiState(
            isLoading = false,
            models = ModelCatalog.models.map { entry ->
                ModelRowState(
                    entry         = entry,
                    isDownloaded  = when (entry.backend) {
                        is ModelBackend.GeminiApi -> true   // always "ready"
                        is ModelBackend.OnDevice  -> repository.isDownloaded(entry)
                    },
                    isActive      = entry.id == activeId,
                    isDownloading = downloadJobs[entry.id]?.isActive == true,
                    downloadPercent = if (downloadJobs[entry.id]?.isActive == true) 0 else null,
                )
            },
        )
    }

    fun download(entry: ModelEntry) {
        // API models need no download — just select
        if (entry.backend is ModelBackend.GeminiApi) { selectModel(entry); return }
        if (downloadJobs[entry.id]?.isActive == true) return

        updateRow(entry.id) { it.copy(isDownloading = true, downloadPercent = 0, errorMessage = null) }
        val job = viewModelScope.launch {
            repository.download(entry).collect { state ->
                when (state) {
                    is DownloadState.Queued     -> updateRow(entry.id) { it.copy(isDownloading = true, downloadPercent = 0) }
                    is DownloadState.InProgress -> updateRow(entry.id) { it.copy(isDownloading = true, downloadPercent = state.percent) }
                    is DownloadState.Done       -> {
                        updateRow(entry.id) { it.copy(isDownloading = false, downloadPercent = null, isDownloaded = true) }
                        if (!mutableState.value.models.any { it.isActive && it.isDownloaded }) selectModel(entry)
                    }
                    is DownloadState.Failed     -> updateRow(entry.id) {
                        it.copy(isDownloading = false, downloadPercent = null, errorMessage = state.reason)
                    }
                }
            }
        }
        downloadJobs[entry.id] = job
    }

    fun cancelDownload(entry: ModelEntry) {
        downloadJobs[entry.id]?.cancel()
        downloadJobs.remove(entry.id)
        repository.deleteModel(entry)
        updateRow(entry.id) { it.copy(isDownloading = false, downloadPercent = null) }
    }

    fun deleteModel(entry: ModelEntry) {
        if (entry.backend !is ModelBackend.OnDevice) return
        val row = mutableState.value.models.find { it.entry.id == entry.id } ?: return
        if (row.isActive) return
        repository.deleteModel(entry)
        updateRow(entry.id) { it.copy(isDownloaded = false) }
    }

    fun selectModel(entry: ModelEntry) {
        repository.setActiveModelId(entry.id)
        mutableState.update { state ->
            state.copy(models = state.models.map { row ->
                row.copy(isActive = row.entry.id == entry.id)
            })
        }
    }

    private fun updateRow(id: String, block: (ModelRowState) -> ModelRowState) {
        mutableState.update { state ->
            state.copy(models = state.models.map { if (it.entry.id == id) block(it) else it })
        }
    }
}
