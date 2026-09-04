package com.example.personaldocumentproject.presentation.library

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaldocumentproject.domain.model.UserSetting
import com.example.personaldocumentproject.presentation.components.DocumentGrid
import com.example.personaldocumentproject.presentation.library.components.LibrarySearchBar
import com.example.personaldocumentproject.presentation.library.components.LibraryTopBar
import com.example.personaldocumentproject.presentation.library.components.RecentSearchSection
import com.example.personaldocumentproject.presentation.library.components.TagFilterSection

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    userSetting: UserSetting,
    actions: LibraryActions,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            LibraryTopBar(
                onSettingClick = actions.navigation.onSettingClick
            )
        },
        modifier = modifier
    ) { padding ->
        if (isLandscape) {
            // Landscape: Side-by-side two-pane layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left pane: Controls (SearchBar, TagFilter, RecentSearches)
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LibrarySearchBar(
                        query = state.searchQuery,
                        onQueryChanged = actions.search.onQueryChanged,
                        onSearch = actions.search.onSearch,
                        onClearQuery = actions.search.onClearQuery
                    )

                    TagFilterSection(
                        availableTags = state.availableTags,
                        filteredTags = state.filteredTags,
                        selectedTags = state.selectedTags,
                        tagQuery = state.tagQuery,
                        isExpanded = state.isTagDropdownExpanded,
                        onToggleExpand = actions.tag.onToggleDropdown,
                        onTagQueryChanged = actions.tag.onQueryChanged,
                        onClearTagQuery = actions.tag.onClearQuery,
                        onTagToggle = actions.tag.onTagToggle,
                        onClearSelectedTags = actions.tag.onClearSelectedTags
                    )

                    if (!state.isSearchActive) {
                        RecentSearchSection()
                    }
                }

                // Right pane: Title and DocumentGrid
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (!state.isSearchActive) "Library" else "Results",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif
                    )

                    DocumentGrid(
                        documents = state.documents,
                        userSetting = userSetting,
                        onDocumentClick = actions.navigation.onDocumentClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // Portrait: Vertical column layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LibrarySearchBar(
                    query = state.searchQuery,
                    onQueryChanged = actions.search.onQueryChanged,
                    onSearch = actions.search.onSearch,
                    onClearQuery = actions.search.onClearQuery
                )

                TagFilterSection(
                    availableTags = state.availableTags,
                    filteredTags = state.filteredTags,
                    selectedTags = state.selectedTags,
                    tagQuery = state.tagQuery,
                    isExpanded = state.isTagDropdownExpanded,
                    onToggleExpand = actions.tag.onToggleDropdown,
                    onTagQueryChanged = actions.tag.onQueryChanged,
                    onClearTagQuery = actions.tag.onClearQuery,
                    onTagToggle = actions.tag.onTagToggle,
                    onClearSelectedTags = actions.tag.onClearSelectedTags
                )

                if (!state.isSearchActive) {
                    RecentSearchSection()

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Library",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif
                    )
                } else {
                    Text(
                        text = "Results",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif
                    )
                }

                DocumentGrid(
                    documents = state.documents,
                    userSetting = userSetting,
                    onDocumentClick = actions.navigation.onDocumentClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
