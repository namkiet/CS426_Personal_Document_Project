package com.example.personaldocumentproject.presentation.modelmanager

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.data.ml.llm.ModelBackend
import com.example.personaldocumentproject.data.ml.llm.ModelEntry

// ── Route ─────────────────────────────────────────────────────────────────────

@Composable
fun ModelManagerRoute(
    viewModel: ModelManagerViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ModelManagerScreen(
        state           = state,
        onBack          = onBack,
        onDownload      = viewModel::download,
        onCancelDownload = viewModel::cancelDownload,
        onSelect        = viewModel::selectModel,
        onDelete        = viewModel::deleteModel,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen(
    state: ModelManagerUiState,
    onBack: () -> Unit,
    onDownload: (ModelEntry) -> Unit,
    onCancelDownload: (ModelEntry) -> Unit,
    onSelect: (ModelEntry) -> Unit,
    onDelete: (ModelEntry) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Models") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val apiModels      = state.models.filter { it.entry.backend is ModelBackend.GeminiApi }
        val onDeviceModels = state.models.filter { it.entry.backend is ModelBackend.OnDevice }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Cloud / API section ───────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(
                    icon  = Icons.Filled.Cloud,
                    label = "Cloud Models",
                    note  = "Instant — no download required. Needs internet + API key.",
                )
            }

            items(apiModels, key = { it.entry.id }) { row ->
                ModelRowCard(
                    row              = row,
                    onDownload       = { onDownload(row.entry) },
                    onCancelDownload = { onCancelDownload(row.entry) },
                    onSelect         = { onSelect(row.entry) },
                    onDelete         = { onDelete(row.entry) },
                )
            }

            // ── On-device section ─────────────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(
                    icon  = Icons.Filled.PhoneAndroid,
                    label = "On-Device Models",
                    note  = "Download once — works fully offline, no API key needed.",
                )
            }

            items(onDeviceModels, key = { it.entry.id }) { row ->
                ModelRowCard(
                    row              = row,
                    onDownload       = { onDownload(row.entry) },
                    onCancelDownload = { onCancelDownload(row.entry) },
                    onSelect         = { onSelect(row.entry) },
                    onDelete         = { onDelete(row.entry) },
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    note: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Model row card ────────────────────────────────────────────────────────────

@Composable
private fun ModelRowCard(
    row: ModelRowState,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    val isApiModel   = row.entry.backend is ModelBackend.GeminiApi
    val borderColor  = if (row.isActive) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.outlineVariant

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (row.isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = if (row.isActive) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (row.isActive) "Active" else "Inactive",
                tint = if (row.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(row.entry.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(row.entry.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ── Metadata chips ────────────────────────────────────────────────────
        if (isApiModel) {
            MetaChip("Backend", "Gemini API")
            MetaChip("Status", "Ready")
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetaChip("Size", formatBytes(row.entry.sizeBytes))
                MetaChip("RAM", "${row.entry.ramRequiredMb} MB")
                if (row.isDownloaded) MetaChip("Status", "On device")
            }
        }

        // ── Error ─────────────────────────────────────────────────────────────
        row.errorMessage?.let {
            Text("Error: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        // ── Download progress (on-device only) ────────────────────────────────
        if (!isApiModel && row.isDownloading) {
            val pct = row.downloadPercent
            if (pct != null && pct > 0) {
                LinearProgressIndicator(progress = { pct / 100f }, modifier = Modifier.fillMaxWidth())
                Text(
                    "$pct%  •  ${formatBytes(row.entry.sizeBytes * pct / 100)} / ${formatBytes(row.entry.sizeBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Queued…", style = MaterialTheme.typography.bodySmall)
            }
        }

        // ── Action buttons ────────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                // API model — just Select / Active badge, no download
                isApiModel -> {
                    SelectOrActiveBadge(isActive = row.isActive, onSelect = onSelect)
                }
                // On-device: downloading
                row.isDownloading -> {
                    OutlinedButton(onClick = onCancelDownload) { Text("Cancel") }
                }
                // On-device: not yet downloaded
                !row.isDownloaded -> {
                    Button(onClick = onDownload) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Download")
                    }
                }
                // On-device: downloaded
                else -> {
                    SelectOrActiveBadge(isActive = row.isActive, onSelect = onSelect)
                    if (!row.isActive) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectOrActiveBadge(isActive: Boolean, onSelect: () -> Unit) {
    if (!isActive) {
        Button(onClick = onSelect) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Use this model")
        }
    } else {
        Button(
            onClick = {},
            enabled = false,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Active")
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun MetaChip(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000_000L -> "%.1f GB".format(bytes / 1_000_000_000.0)
    bytes >= 1_000_000L     -> "%.0f MB".format(bytes / 1_000_000.0)
    else                    -> "%.0f KB".format(bytes / 1_000.0)
}
