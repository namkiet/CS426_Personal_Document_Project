package com.example.personaldocumentproject.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.usecase.SearchDocuments
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val searchDocuments: SearchDocuments) : ViewModel() {
    private val mutableState = kotlinx.coroutines.flow.MutableStateFlow(SearchUiState())
    val uiState = mutableState.asStateFlow()
    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        mutableState.value = mutableState.value.copy(query = query)
        performSearch(100)
    }

    fun performSearch() = performSearch(0)

    fun onDateFilterChanged(filter: DateFilter) {
        mutableState.value = mutableState.value.copy(dateFilter = filter)
        performSearch()
    }

    fun onCategoryFilterChanged(filter: CategoryFilter) {
        mutableState.value = mutableState.value.copy(categoryFilter = filter)
        performSearch()
    }

    private fun performSearch(delayMillis: Long) {
        val query = mutableState.value.query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                delay(delayMillis)
                searchDocuments(query).collectLatest { results ->
                    val cutoff = mutableState.value.dateFilter.ageMillis?.let {
                        System.currentTimeMillis() - it
                    }
                    val category = mutableState.value.categoryFilter.category
                    mutableState.value = mutableState.value.copy(
                        results = results.filter {
                            (cutoff == null || it.document.createdAt >= cutoff) &&
                                (category == null || it.document.category == category)
                        },
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.value = mutableState.value.copy(errorMessage = error.message)
            }
        }
    }
}
