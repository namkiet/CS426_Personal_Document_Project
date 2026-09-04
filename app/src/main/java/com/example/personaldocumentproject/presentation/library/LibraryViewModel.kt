package com.example.personaldocumentproject.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.usecase.ExtractTagsFromDocuments
import com.example.personaldocumentproject.domain.usecase.FilterDocumentsByTags
import com.example.personaldocumentproject.domain.usecase.ObserveDocuments
import com.example.personaldocumentproject.domain.usecase.SearchDocuments
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val observeDocuments: ObserveDocuments,
    private val searchDocuments: SearchDocuments,
    private val extractTagsFromDocuments: ExtractTagsFromDocuments = ExtractTagsFromDocuments(),
    private val filterDocumentsByTags: FilterDocumentsByTags = FilterDocumentsByTags(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var searchJob: Job? = null
    private var searchResults: List<Document> = emptyList()

    init {
        observeAllDocuments()
    }

    private fun observeAllDocuments() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeDocuments().collectLatest { allDocs ->
                val tags = extractTagsFromDocuments(allDocs)
                _uiState.update { current ->
                    val updatedFilteredTags = filterTags(tags, current.tagQuery)
                    current.copy(
                        allDocuments = allDocs,
                        availableTags = tags,
                        filteredTags = updatedFilteredTags,
                    )
                }
                recomputeDisplayedDocuments()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            searchJob?.cancel()
            searchResults = emptyList()
            _uiState.update { it.copy(isSearching = false) }
            recomputeDisplayedDocuments()
        } else {
            performSearch(delayMillis = 300)
        }
    }

    fun performSearch(delayMillis: Long = 0) {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) {
            searchJob?.cancel()
            searchResults = emptyList()
            _uiState.update { it.copy(isSearching = false) }
            recomputeDisplayedDocuments()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSearching = true) }
                if (delayMillis > 0) delay(delayMillis)
                searchDocuments(query).collectLatest { results ->
                    searchResults = results.map { it.document }
                    _uiState.update { it.copy(isSearching = false, errorMessage = null) }
                    recomputeDisplayedDocuments()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(isSearching = false, errorMessage = error.message) }
            }
        }
    }

    fun onClearSearchQuery() {
        searchJob?.cancel()
        searchResults = emptyList()
        _uiState.update { it.copy(searchQuery = "", isSearching = false) }
        recomputeDisplayedDocuments()
    }

    fun onTagQueryChanged(tagQuery: String) {
        _uiState.update { current ->
            val updatedFilteredTags = filterTags(current.availableTags, tagQuery)
            current.copy(
                tagQuery = tagQuery,
                filteredTags = updatedFilteredTags,
            )
        }
    }

    fun onClearTagQuery() {
        _uiState.update { current ->
            current.copy(
                tagQuery = "",
                filteredTags = current.availableTags,
            )
        }
    }

    fun onTagToggle(tag: String) {
        _uiState.update { current ->
            val newSelected = current.selectedTags.toMutableSet()
            val existing = newSelected.firstOrNull { it.equals(tag, ignoreCase = true) }
            if (existing != null) {
                newSelected.remove(existing)
            } else {
                newSelected.add(tag)
            }
            current.copy(selectedTags = newSelected)
        }
        recomputeDisplayedDocuments()
    }

    fun onClearSelectedTags() {
        _uiState.update { it.copy(selectedTags = emptySet()) }
        recomputeDisplayedDocuments()
    }

    fun onToggleTagDropdown() {
        _uiState.update { it.copy(isTagDropdownExpanded = !it.isTagDropdownExpanded) }
    }

    private fun recomputeDisplayedDocuments() {
        val currentState = _uiState.value
        val isQueryActive = currentState.searchQuery.isNotBlank()
        val isTagsActive = currentState.selectedTags.isNotEmpty()
        val isSearchActive = isQueryActive || isTagsActive

        val baseDocs = if (isQueryActive) {
            searchResults
        } else {
            currentState.allDocuments
        }

        val finalDocs = filterDocumentsByTags(baseDocs, currentState.selectedTags)

        _uiState.update { current ->
            current.copy(
                documents = finalDocs,
                isSearchActive = isSearchActive,
            )
        }
    }

    private fun filterTags(tags: List<String>, query: String): List<String> {
        val trimmed = query.trim()
        return if (trimmed.isBlank()) {
            tags
        } else {
            tags.filter { it.contains(trimmed, ignoreCase = true) }
        }
    }
}
