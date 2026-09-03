package com.example.personaldocumentproject.presentation.library.components

import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.domain.model.Document

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentCard(document: Document, onClick: () -> Unit = {}) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().height(150.dp), colors =
        CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary)) {
        Column(Modifier.padding(16.dp)) {
            Text(document.title, style = MaterialTheme.typography.titleMedium)
            Text(document.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
