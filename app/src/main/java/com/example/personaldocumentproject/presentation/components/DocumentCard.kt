package com.example.personaldocumentproject.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.personaldocumentproject.domain.model.Document

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentCard(document: Document, onClick: () -> Unit = {}, config: DocumentCardLayoutConfig) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Card(onClick = onClick, modifier = Modifier.size(width = config.cardWidth, height = config.cardHeight), colors =
        CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary)) {

        if(config.showImage) {
            AsyncImage(
                model = document.imageUri,
                contentDescription = "Document Image",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }else {
            Column(Modifier.padding(16.dp)) {
                Text(document.title, style = MaterialTheme.typography.titleMedium)
                Text(document.description, style = MaterialTheme.typography.bodyMedium)
            }
        }


    }
}
