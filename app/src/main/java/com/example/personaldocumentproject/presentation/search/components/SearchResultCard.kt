package com.example.personaldocumentproject.presentation.search.components

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.domain.model.SearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultCard(result: SearchResult, onClick: () -> Unit = {}) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(result.document.title, style = MaterialTheme.typography.titleMedium)
            Text(result.document.description, style = MaterialTheme.typography.bodyMedium)
            if (LocalContext.current.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                Text(
                    "Semantic: %.2f\nLexical: %.2f\nCombined: %.2f".format(
                        result.semanticScore,
                        result.lexicalScore,
                        result.score,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
