package com.example.personaldocumentproject.presentation.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.personaldocumentproject.domain.model.UserSetting

@Composable
fun LibraryRoute(
    viewModelLibrary: LibraryViewModel,
    userSetting: UserSetting,
    navigationActions: LibraryNavigationActions,
    modifier: Modifier = Modifier,
) {
    val stateLibrary by viewModelLibrary.uiState.collectAsState()

    val actions = remember(viewModelLibrary, navigationActions) {
        LibraryActions(
            search = LibrarySearchActions(
                onQueryChanged = viewModelLibrary::onSearchQueryChanged,
                onSearch = viewModelLibrary::performSearch,
                onClearQuery = viewModelLibrary::onClearSearchQuery,
            ),
            tag = LibraryTagActions(
                onQueryChanged = viewModelLibrary::onTagQueryChanged,
                onClearQuery = viewModelLibrary::onClearTagQuery,
                onTagToggle = viewModelLibrary::onTagToggle,
                onClearSelectedTags = viewModelLibrary::onClearSelectedTags,
                onToggleDropdown = viewModelLibrary::onToggleTagDropdown,
            ),
            navigation = navigationActions,
        )
    }

    LibraryScreen(
        state = stateLibrary,
        userSetting = userSetting,
        actions = actions,
        modifier = modifier,
    )
}
