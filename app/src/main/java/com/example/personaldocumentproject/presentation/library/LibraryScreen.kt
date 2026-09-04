package com.example.personaldocumentproject.presentation.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.model.MockDocuments
import com.example.personaldocumentproject.presentation.library.components.DocumentCard
import com.example.personaldocumentproject.presentation.library.components.categoryColor
import com.example.personaldocumentproject.ui.components.AppTextField
import com.example.personaldocumentproject.ui.theme.*

enum class DateFilter(val label: String) {
    ALL("All Time"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_1_YEAR("Last 1 Year");

    fun matches(createdAt: Long): Boolean {
        val now = System.currentTimeMillis()
        return when (this) {
            ALL -> true
            LAST_7_DAYS -> createdAt >= now - 7L * 24 * 60 * 60 * 1000
            LAST_30_DAYS -> createdAt >= now - 30L * 24 * 60 * 60 * 1000
            LAST_1_YEAR -> createdAt >= now - 365L * 24 * 60 * 60 * 1000
        }
    }
}

@Composable
fun LibraryRoute(
    viewModel: LibraryViewModel,
    onCapture: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onSettingClick: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LibraryScreen(
        state = state,
        onQueryChanged = viewModel::updateQuery,
        onCapture = onCapture,
        onDocumentClick = onDocumentClick,
        onSettingClick = onSettingClick,
        onToggleSelect = viewModel::toggleSelect,
        onSelectAll = viewModel::selectAll,
        onClearSelection = viewModel::clearSelection,
        onDeleteSelected = viewModel::deleteSelected,
    )
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onQueryChanged: (String) -> Unit,
    onCapture: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onSettingClick: () -> Unit = {},
    onToggleSelect: (String) -> Unit = {},
    onSelectAll: (List<String>) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    var selectedCategory by rememberSaveable { mutableStateOf<DocumentCategory?>(null) }
    var selectedDateFilter by rememberSaveable { mutableStateOf(DateFilter.ALL) }

    var categoryMenuOpen by remember { mutableStateOf(false) }
    var dateMenuOpen by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val visible = remember(state.documents, selectedCategory, selectedDateFilter) {
        state.documents.filter { doc ->
            (selectedCategory == null || doc.category == selectedCategory) &&
            selectedDateFilter.matches(doc.createdAt)
        }
    }

    // Auto scroll to top when query changes
    val gridState = rememberLazyGridState()
    LaunchedEffect(state.query) {
        gridState.scrollToItem(0)
    }

    // Handle back button when in selection mode
    BackHandler(enabled = state.isSelectionMode) {
        onClearSelection()
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Documents?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${state.selectedIds.size} selected document(s)? This action cannot be undone.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSelected()
                        showDeleteConfirmDialog = false
                    },
                ) {
                    Text("Delete", color = DeleteColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceRaised,
            shape = RoundedCornerShape(16.dp),
        )
    }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
        containerColor = AppBackground,
        floatingActionButton = {
            if (!state.isSelectionMode) {
                FloatingActionButton(
                    onClick = onCapture,
                    containerColor = AppPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.size(56.dp)
                ) { Icon(Icons.Default.Add, "Add document") }
            }
        },
    ) { insets ->
        Column(Modifier.fillMaxSize().padding(insets)) {
            if (state.isSelectionMode) {
                // Selection Mode Header
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(SurfaceRaised)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onClearSelection) {
                        Icon(Icons.Default.Close, "Clear selection", tint = TextPrimary)
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${state.selectedIds.size} selected",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onSelectAll(visible.map { it.id }) }) {
                        val allSelected = visible.isNotEmpty() && state.selectedIds.containsAll(visible.map { it.id })
                        Icon(
                            if (allSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                            contentDescription = "Select all",
                            tint = if (allSelected) AppPrimary else TextMuted,
                        )
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete selected", tint = DeleteColor)
                    }
                }
            } else {
                // Normal Header
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("PERSONAL", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .7.sp)
                        Text("Documents", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        Modifier
                            .size(38.dp)
                            .background(SurfaceRaised, RoundedCornerShape(12.dp))
                            .border(1.dp, AppBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onSettingClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, "Settings", tint = TextMuted)
                    }
                }

                // Search Bar
                AppTextField(
                    value = state.query,
                    onValueChange = onQueryChanged,
                    singleLine = true,
                    placeholder = "Search your documents...",
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextDim) },
                    trailingIcon = if (state.query.isNotEmpty()) {
                        { IconButton({ onQueryChanged("") }) { Icon(Icons.Default.Close, "Clear", tint = TextMuted) } }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                // 2 Filter Dropdown Buttons
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button 1: Filter Category
                    FilterDropdownButton(
                        icon = Icons.Default.Category,
                        label = selectedCategory?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All Categories",
                        isActive = selectedCategory != null,
                        modifier = Modifier.weight(1f),
                        onClick = { categoryMenuOpen = true }
                    ) {
                        DropdownMenu(
                            expanded = categoryMenuOpen,
                            onDismissRequest = { categoryMenuOpen = false },
                            modifier = Modifier.background(SurfaceRaised)
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Categories", color = if (selectedCategory == null) AppPrimary else TextPrimary, fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    selectedCategory = null
                                    categoryMenuOpen = false
                                }
                            )
                            DocumentCategory.entries.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            cat.name.lowercase().replaceFirstChar { it.uppercase() },
                                            color = if (isSelected) categoryColor(cat) else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Button 2: Filter Date
                    FilterDropdownButton(
                        icon = Icons.Default.DateRange,
                        label = selectedDateFilter.label,
                        isActive = selectedDateFilter != DateFilter.ALL,
                        modifier = Modifier.weight(1f),
                        onClick = { dateMenuOpen = true }
                    ) {
                        DropdownMenu(
                            expanded = dateMenuOpen,
                            onDismissRequest = { dateMenuOpen = false },
                            modifier = Modifier.background(SurfaceRaised)
                        ) {
                            DateFilter.entries.forEach { df ->
                                val isSelected = selectedDateFilter == df
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            df.label,
                                            color = if (isSelected) AppPrimary else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedDateFilter = df
                                        dateMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Error message if any
            state.errorMessage?.let { error ->
                Text(error, color = DeleteColor, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
            }

            // Results Grid or Empty state
            if (visible.isEmpty()) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Search, null, tint = TextDim.copy(alpha = .4f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No documents found", color = TextDim, fontSize = 14.sp)
                    Text("Try changing search or filters", color = TextDim, fontSize = 12.sp)
                }
            } else {
                LazyVerticalGrid(
                    GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(20.dp, 6.dp, 20.dp, 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visible, key = { it.id }) { doc ->
                        DocumentCard(
                            document = doc,
                            searchResult = state.searchResults[doc.id],
                            isSelected = doc.id in state.selectedIds,
                            isSelectionMode = state.isSelectionMode,
                            onClick = {
                                if (state.isSelectionMode) {
                                    onToggleSelect(doc.id)
                                } else {
                                    onDocumentClick(doc.id)
                                }
                            },
                            onLongClick = {
                                onToggleSelect(doc.id)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDropdownButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    menu: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isActive) AppPrimary.copy(alpha = 0.12f) else SurfaceRaised)
                .border(
                    1.dp,
                    if (isActive) AppPrimary else AppBorder,
                    RoundedCornerShape(10.dp)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isActive) AppPrimary else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    color = if (isActive) AppPrimary else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1
                )
            }
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = if (isActive) AppPrimary else TextDim,
                modifier = Modifier.size(18.dp)
            )
        }
        menu()
    }
}

@Preview(name = "Library - Dark", showBackground = true, backgroundColor = 0xFF0D0D12, widthDp = 390, heightDp = 844)
@Composable
private fun LibraryScreenPreview() {
    PersonalDocumentProjectTheme {
        LibraryScreen(
            state = LibraryUiState(MockDocuments.items),
            onQueryChanged = {},
            onCapture = {},
            onDocumentClick = {},
            onSettingClick = {},
        )
    }
}

@Preview(name = "Library - Empty", showBackground = true, backgroundColor = 0xFF0D0D12, widthDp = 390, heightDp = 844)
@Composable
private fun EmptyLibraryScreenPreview() {
    PersonalDocumentProjectTheme {
        LibraryScreen(
            state = LibraryUiState(),
            onQueryChanged = {},
            onCapture = {},
            onDocumentClick = {},
            onSettingClick = {},
        )
    }
}
