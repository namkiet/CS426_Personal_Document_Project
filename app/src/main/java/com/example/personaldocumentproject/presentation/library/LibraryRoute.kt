package com.example.personaldocumentproject.presentation.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.personaldocumentproject.domain.model.UserSetting

@Composable
fun LibraryRoute(
    viewModelLibrary: LibraryViewModel,
    userSetting: UserSetting,
    onDocumentClick: (String) -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
) {
    val stateLibrary by viewModelLibrary.uiState.collectAsState()

    LibraryScreen(
        state = stateLibrary,
        userSetting = userSetting,
        onSearchQueryChanged = viewModelLibrary::onSearchQueryChanged,
        onPerformSearch = viewModelLibrary::performSearch,
        onClearSearchQuery = viewModelLibrary::onClearSearchQuery,
        onTagQueryChanged = viewModelLibrary::onTagQueryChanged,
        onClearTagQuery = viewModelLibrary::onClearTagQuery,
        onTagToggle = viewModelLibrary::onTagToggle,
        onClearSelectedTags = viewModelLibrary::onClearSelectedTags,
        onToggleTagDropdown = viewModelLibrary::onToggleTagDropdown,
        onDocumentClick = onDocumentClick,
        onSettingClick = onSettingClick,
        modifier = modifier
    )
}
