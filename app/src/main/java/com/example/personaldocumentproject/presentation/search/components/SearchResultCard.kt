package com.example.personaldocumentproject.presentation.search.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.domain.model.SearchResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultCard(result: SearchResult, onClick: () -> Unit = {}) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(result.document.title, style = MaterialTheme.typography.titleMedium)
            Text(
                "Created: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(result.document.createdAt))}",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(result.document.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
