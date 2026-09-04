package com.example.personaldocumentproject.presentation.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaldocumentproject.domain.model.*
import com.example.personaldocumentproject.presentation.library.components.DocumentArtwork
import com.example.personaldocumentproject.ui.theme.*
import com.example.personaldocumentproject.ui.components.AppTextField

@Composable
fun ReviewRoute(imageUri: String, viewModel: ReviewViewModel, onSaved: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(imageUri) { viewModel.analyze(imageUri) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onSaved() }
    ReviewScreen(
        state = state,
        onTitleChanged = viewModel::updateTitle,
        onSave = {
            if (imageUri.startsWith("mock://")) onSaved() else viewModel.save()
        },
        onBack = onBack,
    )
}

@Composable
fun ReviewScreen(state: ReviewUiState, onTitleChanged: (String) -> Unit, onSave: () -> Unit, onBack: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val draft = state.draft
    var description by remember(draft?.imageUri) { mutableStateOf(draft?.description.orEmpty()) }
    var category by remember(draft?.imageUri) { mutableStateOf(draft?.category ?: DocumentCategory.NOTE) }
    var tags by remember(draft?.imageUri) { mutableStateOf(draft?.tags.orEmpty()) }
    var objects by remember(draft?.imageUri) { mutableStateOf(draft?.detectedObjects.orEmpty()) }
    var newTag by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    val addTag = {
        if (newTag.isNotBlank()) {
            tags = tags + newTag.trim()
            newTag = ""
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(4.dp)) {
                Icon(Icons.Default.ArrowBack, null); Spacer(Modifier.width(6.dp)); Text("Review Document", fontWeight = FontWeight.SemiBold)
            }
        }
        when {
            state.isAnalyzing -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(color = AppPrimary); Spacer(Modifier.height(16.dp)); Text("Analyzing document...", color = TextMuted) }
            }
            draft != null -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                DocumentArtwork(draft.asDocument(), Modifier.fillMaxWidth().aspectRatio(4f / 3f))
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    ReviewField("TITLE") { AppTextField(draft.title, onTitleChanged, singleLine = true) }
                    ReviewField("DESCRIPTION") { AppTextField(description, { description = it }, minLines = 3) }
                    ReviewField("CATEGORY") {
                        Box { Row(Modifier.fillMaxWidth().background(SurfaceRaised, RoundedCornerShape(8.dp)).border(1.dp, AppBorder, RoundedCornerShape(8.dp))
                            .clickable { categoryMenu = true }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(category.name.lowercase().replaceFirstChar { it.uppercase() }, color = TextPrimary, modifier = Modifier.weight(1f)); Icon(Icons.Default.KeyboardArrowDown, null, tint = TextDim)
                        }
                        DropdownMenu(categoryMenu, { categoryMenu = false }, containerColor = SurfaceRaised) {
                            listOf(DocumentCategory.STUDY, DocumentCategory.RECEIPT, DocumentCategory.NOTE).forEach { item ->
                                DropdownMenuItem(text = { Text(item.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { category = item; categoryMenu = false })
                            }
                        } }
                    }
                    ReviewField("DETECTED OBJECTS") {
                        Column(Modifier.fillMaxWidth().background(SurfaceRaised, RoundedCornerShape(8.dp)).border(1.dp, AppBorder, RoundedCornerShape(8.dp)).padding(horizontal = 14.dp)) {
                            objects.forEachIndexed { index, item -> Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(item, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f)); Icon(Icons.Default.Close, "Remove", tint = TextDim,
                                    modifier = Modifier.size(19.dp).clickable { objects = objects - item })
                            }; if (index < objects.lastIndex) HorizontalDivider(color = AppBorder) }
                        }
                    }
                    ReviewField("TAGS") {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            tags.forEach { tag -> Row(Modifier.background(SurfaceRaised, RoundedCornerShape(100.dp)).border(1.dp, AppBorder, RoundedCornerShape(100.dp)).padding(9.dp, 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(tag, color = TextMuted, fontSize = 12.sp); Spacer(Modifier.width(5.dp)); Text("×", color = TextDim, modifier = Modifier.clickable { tags = tags - tag })
                            } }
                        }
                        Spacer(Modifier.height(8.dp)); Row(verticalAlignment = Alignment.CenterVertically) {
                            AppTextField(
                                value = newTag,
                                onValueChange = { newTag = it },
                                modifier = Modifier.weight(1f),
                                placeholder = "Add a tag",
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { addTag(); focusManager.clearFocus() }),
                            )
                            Spacer(Modifier.width(8.dp)); IconButton(onClick = addTag,
                                modifier = Modifier.background(AppPrimary, RoundedCornerShape(8.dp))) { Icon(Icons.Default.Add, "Add tag", tint = Color.White) }
                        }
                    }
                    Column(Modifier.fillMaxWidth().background(SurfaceRaised, RoundedCornerShape(8.dp)).border(1.dp, AppBorder, RoundedCornerShape(8.dp))) {
                        Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("OCR Text", color = TextMuted, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); Icon(Icons.Default.KeyboardArrowDown, null, tint = TextDim, modifier = Modifier.rotate(if (expanded) 180f else 0f))
                        }
                        AnimatedVisibility(expanded) { Text(draft.ocrText, color = TextMuted, fontSize = 12.sp, lineHeight = 20.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) }
                    }
                    Button(onClick = onSave, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)) {
                        if (state.isSaving) { CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(9.dp)) }
                        Text(if (state.isSaving) "Saving..." else "Confirm & Save", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.errorMessage ?: "Unable to analyze document", color = TextMuted) }
        }
    }
}

@Composable private fun ReviewField(label: String, content: @Composable () -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .8.sp); content()
}

private fun DocumentDraft.asDocument() = Document("preview", imageUri, title, description, ocrText, category, tags, detectedObjects, emptyList(), 0L)
private fun mockState(saving: Boolean = false) = ReviewUiState(draft = MockDocuments.items.first().let { DocumentDraft(it.imageUri, it.title, it.description, it.ocrText, it.category, it.tags, it.detectedObjects) }, isSaving = saving)

@Preview(name="Review - Filled", showBackground=true, widthDp=390, heightDp=844) @Composable private fun ReviewPreview() = PersonalDocumentProjectTheme { ReviewScreen(mockState(), {}, {}, {}) }
@Preview(name="Review - Analyzing", showBackground=true, widthDp=390, heightDp=844) @Composable private fun AnalyzingPreview() = PersonalDocumentProjectTheme { ReviewScreen(ReviewUiState(isAnalyzing=true), {}, {}, {}) }
@Preview(name="Review - Saving", showBackground=true, widthDp=390, heightDp=844) @Composable private fun SavingPreview() = PersonalDocumentProjectTheme { ReviewScreen(mockState(true), {}, {}, {}) }
