package com.example.personaldocumentproject.presentation.library

import android.R
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.presentation.library.components.DocumentCard
import com.example.personaldocumentproject.presentation.library.components.CaptureButton

@Composable
fun LibraryRoute(viewModel: LibraryViewModel, onCapture: () -> Unit, onSearch: () -> Unit, onDocumentClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    LibraryScreen(state, onCapture, onSearch, onDocumentClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(state: LibraryUiState, onCapture: () -> Unit, onSearch: () -> Unit, onDocumentClick: (String) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(
            title =  {Text(
                text = "Library",
                style = MaterialTheme.typography.bodyLarge,
            )}
        )}
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedButton(onClick = onSearch, modifier = Modifier.fillMaxWidth()) { Text("Search library") }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Search Section Header
            Text(
                text = "Recent Search",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Scrollable Items for Recent Search

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //Items
            }


            Spacer(modifier = Modifier.height(24.dp))

            // Main Library Section Header
            Text(
                text = "Library",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grid Items for Main Library
            if (state.documents.isEmpty()) Text("Your document library is empty.")
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items = state.documents, key = Document::id) { document ->
                    DocumentCard(
                        document = document,
                        onClick = { onDocumentClick(document.id) }
                    )
                }
            }

        }

        CaptureButton(
            onClick = onCapture
        )

    }


//    Scaffold(topBar = { TopAppBar(title = { Text("Personal Document") }) }) { padding ->
//        Column(
//            Modifier.fillMaxSize().padding(padding).padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp),
//        ) {
//            Button(onClick = onCapture, modifier = Modifier.fillMaxWidth()) { Text("Capture document") }
//            OutlinedButton(onClick = onSearch, modifier = Modifier.fillMaxWidth()) { Text("Search library") }
//            if (state.documents.isEmpty()) Text("Your document library is empty.")
//            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                items(state.documents, key = Document::id) { document ->
//                    DocumentCard(
//                        document = document,
//                        onClick = { onDocumentClick(document.id) }
//                    )
//                }
//            }
//        }
//    }


}

@Composable
fun SquarePlaceholder() {
    TODO("Not yet implemented")
}
