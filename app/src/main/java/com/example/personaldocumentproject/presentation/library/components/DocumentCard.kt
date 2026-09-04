package com.example.personaldocumentproject.presentation.library.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.model.MockDocuments
import com.example.personaldocumentproject.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun categoryColor(category: DocumentCategory): Color = when (category) {
    DocumentCategory.STUDY -> StudyColor
    DocumentCategory.RECEIPT -> ReceiptColor
    DocumentCategory.NOTE -> NoteColor
    else -> TextMuted
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
    Box(modifier.background(SurfaceRaised)) {
        if (document.imageUri.isNotBlank()) AsyncImage(model = document.imageUri, contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Canvas(Modifier.fillMaxSize()) {
            drawCircle(accent.copy(alpha = .17f), size.minDimension * .72f, Offset(size.width, 0f))
            drawCircle(accent.copy(alpha = .09f), size.minDimension * .58f, Offset(0f, size.height))
            repeat(4) { index ->
                val y = size.height * (.28f + index * .14f)
                drawLine(if (index == 0) accent.copy(alpha = .75f) else TextMuted.copy(alpha = .38f),
                    Offset(size.width * .16f, y), Offset(size.width * if (index == 0) .62f else .82f, y),
                    if (index == 0) 7f else 4f)
            }
        }
    }
}

@Composable
fun DocumentCard(document: Document, onClick: () -> Unit = {}) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppSurface)
        .border(1.dp, AppBorder, RoundedCornerShape(14.dp)).clickable(onClick = onClick)) {
        DocumentArtwork(document, Modifier.fillMaxWidth().aspectRatio(4f / 3f))
        Column(Modifier.padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 12.dp)) {
            CategoryBadge(document.category)
            Spacer(Modifier.height(8.dp))
            Text(document.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(document.createdAt)),
                color = TextDim, fontSize = 11.sp)
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
