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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaldocumentproject.domain.model.UserSetting
import com.example.personaldocumentproject.presentation.components.DocumentGrid
import com.example.personaldocumentproject.presentation.search.components.SearchBar
import com.example.personaldocumentproject.presentation.search.components.searchResultConverter
import com.example.personaldocumentproject.ui.imageVector.FluentuiSystemIconsPerson
import com.example.personaldocumentproject.ui.imageVector.FluentuiSystemIconsSettings

@Composable
fun SearchRoute(viewModelSearch: SearchViewModel, userSetting: UserSetting,
                onBack: () -> Unit, onDocumentClick: (String) -> Unit,
                onAccountClick: () -> Unit,onSettingClick: () -> Unit) {
    val stateSearch by viewModelSearch.uiState.collectAsState()
    SearchScreen(
        state = stateSearch,
        userSetting = userSetting,
        onQueryChanged = viewModelSearch::onQueryChanged,
        onSearch = viewModelSearch::performSearch,
        onClearQuery = viewModelSearch::onClearQuery,
        onBack = onBack,
        onDocumentClick = onDocumentClick,
        onAccountClick = onAccountClick,
        onSettingClick = onSettingClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    userSetting: UserSetting,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    onClearQuery: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onAccountClick:() -> Unit,
    onSettingClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(
            title =  {Text(
                text = "Search",
                //style = MaterialTheme.typography.bodyLarge,
            )},
            navigationIcon = {
                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }
            },
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
            SearchBar(
                state = state,
                onQueryChanged = onQueryChanged,
                onSearch = onSearch,
                onClearQuery = onClearQuery
            )

            //Spacer(modifier = Modifier.height(24.dp))

            // Results of search
            Text(
                text = "Results",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif
            )

            //Spacer(modifier = Modifier.height(12.dp))

            DocumentGrid(
                documents = searchResultConverter(state.results),
                userSetting = userSetting,
                onDocumentClick = onDocumentClick
            )

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