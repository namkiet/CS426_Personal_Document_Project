package com.example.personaldocumentproject.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaldocumentproject.presentation.library.components.FakeSearchBar
import com.example.personaldocumentproject.presentation.search.components.SearchBar
import com.example.personaldocumentproject.presentation.search.components.SearchResultCard

@Composable
fun SearchRoute(viewModel: SearchViewModel, onBack: () -> Unit, onDocumentClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    SearchScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onSearch = viewModel::performSearch,
        onClearQuery = viewModel::onClearQuery,
        onBack = onBack,
        onDocumentClick = onDocumentClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    onClearQuery: () -> Unit,
    onDocumentClick: (String) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(
            title =  {Text(
                text = "Search",
                style = MaterialTheme.typography.bodyLarge,
            )}
        )}
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchBar(
                state = state,
                onQueryChanged = onQueryChanged,
                onSearch = onSearch,
                onClearQuery = onClearQuery
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Results of search
            Text(
                text = "Results",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.results, key = { it.document.id }) { result ->
                    SearchResultCard(
                        result = result,
                        onClick = { onDocumentClick(result.document.id) }
                    )
                }
            }

                //..
            //search history text + back button
        }
    }
}


//Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
//        Column(
//            Modifier.fillMaxSize().padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp),
//        ) {
//            SearchBar(state = state, onQueryChanged = onQueryChanged, onSearch = onSearch, onClearQuery = onClearQuery)
//            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
//                items(state.results, key = { it.document.id }) { result ->
//                    SearchResultCard(
//                        result = result,
//                        onClick = { onDocumentClick(result.document.id) }
//                    )
//                }
//            }
//            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back", color = Color.White) }
//        }
//    }