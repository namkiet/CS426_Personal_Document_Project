package com.example.personaldocumentproject.presentation.library.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.personaldocumentproject.BuildConfig
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.model.MockDocuments
import com.example.personaldocumentproject.domain.model.SearchResult
import com.example.personaldocumentproject.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun categoryColor(category: DocumentCategory): Color = when (category) {
    DocumentCategory.STUDY -> StudyColor
    DocumentCategory.RECEIPT -> ReceiptColor
    DocumentCategory.NOTE -> NoteColor
    DocumentCategory.IDENTITY -> IdentityColor
    DocumentCategory.WORK -> WorkColor
    DocumentCategory.OTHER -> TextMuted
}

@Composable
fun CategoryBadge(category: DocumentCategory) {
    val color = categoryColor(category)
    Text(category.name, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = .4.sp, modifier = Modifier.clip(RoundedCornerShape(100.dp))
            .background(color.copy(alpha = .13f)).padding(horizontal = 8.dp, vertical = 4.dp))
}

@Composable
fun DocumentArtwork(document: Document, modifier: Modifier = Modifier) {
    val accent = categoryColor(document.category)
    val mutedColor = TextMuted
    val raisedColor = SurfaceRaised
    Box(modifier.background(raisedColor)) {
        if (document.imageUri.isNotBlank()) AsyncImage(model = document.imageUri, contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Canvas(Modifier.fillMaxSize()) {
            drawCircle(accent.copy(alpha = .17f), size.minDimension * .72f, Offset(size.width, 0f))
            drawCircle(accent.copy(alpha = .09f), size.minDimension * .58f, Offset(0f, size.height))
            repeat(4) { index ->
                val y = size.height * (.28f + index * .14f)
                drawLine(if (index == 0) accent.copy(alpha = .75f) else mutedColor.copy(alpha = .38f),
                    Offset(size.width * .16f, y), Offset(size.width * if (index == 0) .62f else .82f, y),
                    if (index == 0) 7f else 4f)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentCard(
    document: Document,
    searchResult: SearchResult? = null,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val borderColor = if (isSelected) AppPrimary else if (isSelectionMode) AppBorder.copy(alpha = 0.6f) else AppBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) AppPrimary.copy(alpha = 0.08f) else AppSurface)
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
            DocumentArtwork(document, Modifier.fillMaxSize())

            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(if (isSelected) AppPrimary else Color.Black.copy(alpha = 0.45f))
                        .border(1.5.dp, if (isSelected) AppPrimary else Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
        Column(Modifier.padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 12.dp)) {
            CategoryBadge(document.category)
            Spacer(Modifier.height(8.dp))
            Text(document.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(document.createdAt)),
                color = TextDim, fontSize = 11.sp)
            // Debug-only: show search scores to inspect ranking behavior without exposing internals in release builds.
            if (BuildConfig.DEBUG && searchResult != null) {
                Spacer(Modifier.height(6.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(SurfaceRaised, RoundedCornerShape(6.dp))
                        .border(1.dp, AppBorder, RoundedCornerShape(6.dp))
                        .padding(6.dp)
                ) {
                    Text("⚡ %.3f".format(searchResult.score), color = AppPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("🔤 lex: %.3f".format(searchResult.lexicalScore), color = TextMuted, fontSize = 9.sp)
                    Text("🧠 sem: %.3f".format(searchResult.semanticScore), color = TextDim, fontSize = 9.sp)
                }
            }
        }
    }
}

@Preview(name = "Study document card", showBackground = true, backgroundColor = 0xFF0D0D12, widthDp = 180)
@Composable
private fun DocumentCardPreview() {
    PersonalDocumentProjectTheme {
        Box(Modifier.padding(8.dp)) {
            DocumentCard(MockDocuments.items.first())
        }
    }
}
