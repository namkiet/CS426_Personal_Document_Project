package com.example.personaldocumentproject.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.UserSetting
import com.example.personaldocumentproject.presentation.components.DocumentCard
import com.example.personaldocumentproject.presentation.components.DocumentGrid
import com.example.personaldocumentproject.presentation.library.components.FakeSearchBar
import com.example.personaldocumentproject.ui.imageVector.*

@Composable
fun LibraryRoute(viewModelLibrary: LibraryViewModel, userSetting: UserSetting,
                 onSearch: () -> Unit, onDocumentClick: (String) -> Unit,
                 onAccountClick: () -> Unit, onSettingClick: () -> Unit) {
    val stateLibrary by viewModelLibrary.uiState.collectAsState()
    LibraryScreen(stateLibrary, userSetting = userSetting, onSearch = onSearch,onDocumentClick, onAccountClick, onSettingClick )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(stateLibrary: LibraryUiState, userSetting: UserSetting,
                  onSearch: () -> Unit, onDocumentClick: (String) -> Unit,
                  onAccountClick: () -> Unit, onSettingClick: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(
            title =  {Text(
                text = "Library",
                //style = MaterialTheme.typography.bodyLarge,
            )},
            actions = {
                IconButton(onClick = onAccountClick) {
                    Icon(
                        imageVector = FluentuiSystemIconsPerson,
                        contentDescription = "account profile"
                    )
                }
                IconButton(onClick = onSettingClick) {
                    Icon(
                        imageVector = FluentuiSystemIconsSettings,
                        contentDescription = "setting"
                    )
                }
            }
        )}
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FakeSearchBar { onSearch() }


            //OutlinedButton(onClick = onSearch, modifier = Modifier.fillMaxWidth()) { Text("Search library") }

            //Spacer(modifier = Modifier.height(8.dp))

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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
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

            //Spacer(modifier = Modifier.height(12.dp))

            // Grid Items for Main Library
            DocumentGrid(
                documents = stateLibrary.documents,
                userSetting = userSetting,
                onDocumentClick = onDocumentClick
            )

        }
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
