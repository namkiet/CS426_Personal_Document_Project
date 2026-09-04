package com.example.personaldocumentproject.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    onSearchQueryChanged: (String) -> Unit,
    onPerformSearch: () -> Unit,
    onClearSearchQuery: () -> Unit,
    onTagQueryChanged: (String) -> Unit,
    onClearTagQuery: () -> Unit,
    onTagToggle: (String) -> Unit,
    onClearSelectedTags: () -> Unit,
    onToggleTagDropdown: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            LibraryTopBar(
                onSettingClick = onSettingClick
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Embedded workable search bar
            LibrarySearchBar(
                query = state.searchQuery,
                onQueryChanged = onSearchQueryChanged,
                onSearch = onPerformSearch,
                onClearQuery = onClearSearchQuery
            )

            // Push-down tag filtering system
            TagFilterSection(
                availableTags = state.availableTags,
                filteredTags = state.filteredTags,
                selectedTags = state.selectedTags,
                tagQuery = state.tagQuery,
                isExpanded = state.isTagDropdownExpanded,
                onToggleExpand = onToggleTagDropdown,
                onTagQueryChanged = onTagQueryChanged,
                onClearTagQuery = onClearTagQuery,
                onTagToggle = onTagToggle,
                onClearSelectedTags = onClearSelectedTags
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

            // Results or whole library grid - pushed down when dropdown expands
            DocumentGrid(
                documents = state.documents,
                userSetting = userSetting,
                onDocumentClick = onDocumentClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
