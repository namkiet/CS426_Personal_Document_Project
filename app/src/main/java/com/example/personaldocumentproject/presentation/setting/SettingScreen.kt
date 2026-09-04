package com.example.personaldocumentproject.presentation.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personaldocumentproject.ui.theme.*

@Composable
fun SettingRoute(
    viewModel: SettingViewModel,
    onBack: () -> Unit,
) {
    SettingsScreen(
        viewModel = viewModel,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.resetMessage) {
        state.resetMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearResetMessage()
        }
    }

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // ── Section 1: Preferences ───────────────────────────────────────
            item {
                SectionTitle(title = "App Preferences")
                Spacer(modifier = Modifier.height(10.dp))

                CardContainer {
                    SettingSwitchRow(
                        icon = Icons.Default.DarkMode,
                        iconTint = AppPrimary,
                        title = "Dark Mode",
                        subtitle = "Enable dark theme for the interface",
                        checked = state.isDarkMode,
                        onCheckedChange = viewModel::setDarkMode,
                    )

                    SettingDivider()

                    SettingSwitchRow(
                        icon = Icons.Default.CameraAlt,
                        iconTint = StudyColor,
                        title = "Start Camera Mode",
                        subtitle = "Open document scanner directly on app launch",
                        checked = state.startCameraMode,
                        onCheckedChange = viewModel::setStartCameraMode,
                    )

                    SettingDivider()

                    SettingSwitchRow(
                        icon = Icons.Default.CloudOff,
                        iconTint = ReceiptColor,
                        title = "Disable Cloud API",
                        subtitle = "Run 100% on-device (ML Kit OCR & heuristics)",
                        checked = state.disableApiMode,
                        onCheckedChange = viewModel::setDisableApiMode,
                    )
                }
            }

            // ── Section 2: Data & Storage ────────────────────────────────────
            item {
                SectionTitle(title = "Data & Storage")
                Spacer(modifier = Modifier.height(10.dp))

                CardContainer {
                    SettingActionRow(
                        icon = Icons.Default.DeleteForever,
                        iconTint = DeleteColor,
                        title = "Factory Reset",
                        titleColor = DeleteColor,
                        subtitle = "Delete all documents, images and reset settings",
                        onClick = { viewModel.showResetDialog(true) },
                    )
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── Factory Reset Confirmation Dialog ────────────────────────────────────
    if (state.showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showResetDialog(false) },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = DeleteColor,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Factory Reset",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently erase all scanned documents, files, and reset all app settings? This action cannot be undone.",
                    color = TextMuted,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::performFactoryReset,
                    colors = ButtonDefaults.buttonColors(containerColor = DeleteColor),
                ) {
                    Text("Delete Everything", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showResetDialog(false) }) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = SurfaceRaised,
            shape = RoundedCornerShape(20.dp),
        )
    }
}

// ── UI Helper Components ──────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextDim,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun CardContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceRaised)
            .border(1.dp, AppBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 4.dp),
        content = content,
    )
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = AppBorder,
    )
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp,
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppPrimary,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = AppBorder,
            )
        )
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    titleColor: Color = TextPrimary,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 18.sp,
            )
        }
    }
}