package com.example.personaldocumentproject.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.usecase.ObserveDocuments
import com.example.personaldocumentproject.domain.usecase.SearchDocuments
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    observeDocuments: ObserveDocuments,
    searchDocuments: SearchDocuments,
) : ViewModel() {
    private val query = MutableStateFlow("")

    private val searchState = query.debounce(250).flatMapLatest { currentQuery ->
        if (currentQuery.isBlank()) {
            observeDocuments().map { LibraryUiState(documents = it) }
        } else {
            flow { emitAll(searchDocuments(currentQuery)) }
                .map { results -> LibraryUiState(
                    documents = results.map { it.document },
                    query = currentQuery,
                    searchResults = results.associateBy { it.document.id },
                ) }
                .catch { error -> emit(LibraryUiState(query = currentQuery, errorMessage = error.message)) }
        }
    }

    val uiState = combine(query, searchState) { currentQuery, result ->
        result.copy(query = currentQuery)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun updateQuery(value: String) {
        query.value = value
    }
}
