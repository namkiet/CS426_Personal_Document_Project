package com.example.personaldocumentproject.presentation.library

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

@Composable
fun LibraryRoute(
    viewModel: LibraryViewModel,
    onImageCaptured: (String) -> Unit,
    onDocumentClick: (String) -> Unit,
    onSettingClick: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LibraryScreen(state, viewModel::updateQuery, onImageCaptured, onDocumentClick, onSettingClick)
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onQueryChanged: (String) -> Unit,
    onImageCaptured: (String) -> Unit,
    onDocumentClick: (String) -> Unit,
    onSettingClick: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = context.findActivity()
    var category by rememberSaveable { mutableStateOf<DocumentCategory?>(null) }
    val visible = remember(state.documents, category) {
        state.documents.filter { category == null || it.category == category }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            onImageCaptured(uri.toString())
        }
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = runCatching {
                result.data?.let(GmsDocumentScanningResult::fromActivityResultIntent)
            }.getOrNull()
            val uri = scanResult?.pages?.firstOrNull()?.imageUri?.toString()
            if (uri != null) {
                onImageCaptured(uri)
            }
        }
    }

    fun launchScanner() {
        val currentActivity = activity ?: run {
            picker.launch("image/*")
            return
        }
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        GmsDocumentScanning.getClient(options).getStartScanIntent(currentActivity)
            .addOnSuccessListener { sender ->
                runCatching {
                    scannerLauncher.launch(IntentSenderRequest.Builder(sender).build())
                }.onFailure {
                    picker.launch("image/*")
                }
            }
            .addOnFailureListener {
                picker.launch("image/*")
            }
    }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
        containerColor = AppBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launchScanner() },
                containerColor = AppPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.size(56.dp)
            ) { Icon(Icons.Default.Add, "Add document") }
        },
    ) { insets ->
        Column(Modifier.fillMaxSize().padding(insets)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("PERSONAL", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .7.sp)
                    Text("Documents", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.size(38.dp).background(SurfaceRaised, RoundedCornerShape(12.dp))
                    .border(1.dp, AppBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = onSettingClick), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, "Settings", tint = TextMuted)
                }
            }
            AppTextField(value = state.query, onValueChange = onQueryChanged, singleLine = true,
                placeholder = "Search your documents...",
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextDim) },
                trailingIcon = if (state.query.isNotEmpty()) {{ IconButton({ onQueryChanged("") }) { Icon(Icons.Default.Close, "Clear", tint = TextMuted) } }} else null,
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill("All", null, category) { category = null }
                DocumentCategory.entries.forEach { item ->
                    FilterPill(item.name.lowercase().replaceFirstChar { it.uppercase() }, item, category) { category = item }
                }
            }
            Text(if (state.query.isBlank()) "RECENT" else "${visible.size} RESULTS FOR \"${state.query}\"", color = TextDim,
                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .5.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
            state.errorMessage?.let { error ->
                Text(error, color = DeleteColor, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp))
            }
            if (visible.isEmpty()) Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Search, null, tint = TextDim.copy(alpha = .4f), modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp)); Text("No documents found", color = TextDim, fontSize = 14.sp)
                Text("Try another search or category", color = TextDim, fontSize = 12.sp)
            } else LazyVerticalGrid(GridCells.Fixed(2), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(visible, key = { it.id }) { doc -> DocumentCard(doc, state.searchResults[doc.id]) { onDocumentClick(doc.id) } }
            }
        }
    }
}

@Composable private fun FilterPill(label: String, value: DocumentCategory?, selected: DocumentCategory?, onClick: () -> Unit) {
    val active = value == selected
    val accent = value?.let(::categoryColor) ?: AppPrimary
    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (active) Color.White else if (value == null) TextMuted else accent,
        modifier = Modifier.background(if (active) accent else if (value == null) SurfaceRaised else accent.copy(alpha = .13f), RoundedCornerShape(100.dp))
            .border(1.dp, if (active || value != null) accent.copy(alpha = .8f) else AppBorder, RoundedCornerShape(100.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 7.dp))
}

@Preview(name = "Library - Dark", showBackground = true, backgroundColor = 0xFF0D0D12, widthDp = 390, heightDp = 844)
@Composable
private fun LibraryScreenPreview() {
    PersonalDocumentProjectTheme {
        LibraryScreen(
            state = LibraryUiState(MockDocuments.items),
            onQueryChanged = {},
            onImageCaptured = {},
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
            onImageCaptured = {},
            onDocumentClick = {},
            onSettingClick = {},
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
