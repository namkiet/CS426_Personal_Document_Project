package com.example.personaldocumentproject.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.usecase.SearchDocuments
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val searchDocuments: SearchDocuments) : ViewModel() {
    private val mutableState = kotlinx.coroutines.flow.MutableStateFlow(SearchUiState())
    val uiState = mutableState.asStateFlow()
    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        mutableState.value = mutableState.value.copy(query = query)
    }

    fun onClearQuery() {
        mutableState.value = mutableState.value.copy(query = "")
    }

    fun performSearch() {
        val query = mutableState.value.query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                searchDocuments(query).collectLatest { results ->
                    mutableState.value = mutableState.value.copy(results = results)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.value = mutableState.value.copy(errorMessage = error.message)
            }
        }
    }
}
