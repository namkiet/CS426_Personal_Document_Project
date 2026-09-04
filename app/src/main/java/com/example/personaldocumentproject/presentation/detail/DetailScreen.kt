package com.example.personaldocumentproject.presentation.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.personaldocumentproject.domain.model.MockDocuments
import com.example.personaldocumentproject.presentation.library.components.CategoryBadge
import com.example.personaldocumentproject.presentation.library.components.DocumentArtwork
import com.example.personaldocumentproject.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailRoute(documentId: String, viewModel: DetailViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(documentId) { viewModel.loadDocument(documentId) }
    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }
    DetailScreen(state, onBack, viewModel::delete)
}

@Composable
fun DetailScreen(state: DetailUiState, onBack: () -> Unit, onDelete: () -> Unit) {
    var ocrExpanded by remember { mutableStateOf(false) }
    Surface(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = AppBackground,
    ) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppPrimary)
            }
            state.document != null -> {
                val document = state.document
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 4.dp)) {
                            Icon(Icons.Default.ArrowBack, null); Spacer(Modifier.width(6.dp))
                            Text(document.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        DocumentArtwork(document, Modifier.fillMaxWidth().aspectRatio(4f / 3f))
                        Column(Modifier.padding(20.dp)) {
                            Text(document.title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CategoryBadge(document.category); Text("  •  ", color = TextDim)
                                Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(document.createdAt)),
                                    color = TextDim, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        HorizontalDivider(color = AppBorder)
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Text(document.description, color = TextMuted, fontSize = 14.sp, lineHeight = 24.sp)
                            if (document.tags.isNotEmpty()) Section("TAGS") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { document.tags.forEach { Tag(it) } }
                            }
                            if (document.detectedObjects.isNotEmpty()) Section("DETECTED OBJECTS") {
                                Text(document.detectedObjects.joinToString("  •  "), color = TextMuted, fontSize = 14.sp)
                            }
                            Column(Modifier.fillMaxWidth().background(SurfaceRaised, RoundedCornerShape(8.dp))
                                .border(1.dp, AppBorder, RoundedCornerShape(8.dp))) {
                                Row(Modifier.fillMaxWidth().clickable { ocrExpanded = !ocrExpanded }.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text("OCR Text", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.KeyboardArrowDown, null, tint = TextDim, modifier = Modifier.rotate(if (ocrExpanded) 180f else 0f))
                                }
                                AnimatedVisibility(ocrExpanded) {
                                    Text(document.ocrText.ifBlank { "No text detected." }, color = TextMuted, fontSize = 12.sp,
                                        lineHeight = 20.sp, fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp))
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(onClick = onDelete, enabled = !state.isDeleting, shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DeleteColor.copy(alpha = .3f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeleteColor)) {
                                    if (state.isDeleting) {
                                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = DeleteColor)
                                    } else {
                                        Icon(Icons.Default.Delete, null, Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(7.dp))
                                    Text(if (state.isDeleting) "Deleting..." else "Delete")
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }
            else -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text(state.errorMessage ?: "Document not found", color = TextMuted)
                TextButton(onClick = onBack) { Text("Back to library") }
            }
        }
    }
}

@Composable private fun Section(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .8.sp)
        content()
    }
}

@Composable private fun Tag(text: String) {
    Text(text, color = TextMuted, fontSize = 12.sp, modifier = Modifier.background(SurfaceRaised, RoundedCornerShape(100.dp))
        .border(1.dp, AppBorder, RoundedCornerShape(100.dp)).padding(horizontal = 10.dp, vertical = 6.dp))
}

@Preview(name = "Document Detail - Dark", showBackground = true, backgroundColor = 0xFF0D0D12, widthDp = 390, heightDp = 844)
@Composable
private fun DetailScreenPreview() {
    PersonalDocumentProjectTheme {
        DetailScreen(
            state = DetailUiState(document = MockDocuments.items.first(), isLoading = false),
            onBack = {},
            onDelete = {},
        )
    }
}
