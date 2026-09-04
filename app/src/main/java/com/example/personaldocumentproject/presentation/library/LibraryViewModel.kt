package com.example.personaldocumentproject.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldocumentproject.domain.usecase.ObserveDocuments
import com.example.personaldocumentproject.domain.model.MockDocuments
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class LibraryViewModel(observeDocuments: ObserveDocuments) : ViewModel() {
    val uiState = observeDocuments()
        .map { LibraryUiState(it.ifEmpty { MockDocuments.items }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())
}
