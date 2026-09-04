package com.example.personaldocumentproject.presentation.library

/**
 * Encapsulates search-related callbacks for the library screen.
 */
data class LibrarySearchActions(
    val onQueryChanged: (String) -> Unit,
    val onSearch: () -> Unit,
    val onClearQuery: () -> Unit,
)

/**
 * Encapsulates tag filtering callbacks for the library screen.
 */
data class LibraryTagActions(
    val onQueryChanged: (String) -> Unit,
    val onClearQuery: () -> Unit,
    val onTagToggle: (String) -> Unit,
    val onClearSelectedTags: () -> Unit,
    val onToggleDropdown: () -> Unit,
)

/**
 * Encapsulates navigation callbacks originating from the library screen.
 */
data class LibraryNavigationActions(
    val onDocumentClick: (String) -> Unit,
    val onSettingClick: () -> Unit,
)

/**
 * Unified actions container composed of narrower action classes.
 */
data class LibraryActions(
    val search: LibrarySearchActions,
    val tag: LibraryTagActions,
    val navigation: LibraryNavigationActions,
)
