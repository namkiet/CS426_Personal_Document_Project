package com.example.personaldocumentproject.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.UserSetting

@Composable
fun DocumentGrid(
    documents: List<Document>,
    userSetting: UserSetting,
    onDocumentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val layoutConfig = userSetting.toLayoutConfig()

    if (documents.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Your document library is empty.")
        }
        return
    }

    if (isLandscape) {
        // LANDSCAPE: Horizontal Scrolling Grid
        LazyHorizontalGrid(
            rows = GridCells.Adaptive(minSize = layoutConfig.cardHeight),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = 8.dp, end = 120.dp,
                top = 8.dp, bottom = 8.dp
            ),
            modifier = modifier.fillMaxSize(),
        ) {
            items(
                items = documents,
                key = { it.id }
            ) { document ->
                DocumentCard(
                    document = document,
                    config = layoutConfig,
                    onClick = { onDocumentClick(document.id) }
                )
            }
        }
    } else {
        // PORTRAIT: Vertical Scrolling Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = layoutConfig.cardWidth),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp, start = 12.dp, end = 12.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(
                items = documents,
                key = { it.id }
            ) { document ->
                DocumentCard(
                    document = document,
                    config = layoutConfig,
                    onClick = { onDocumentClick(document.id) }
                )
            }
        }
    }
}