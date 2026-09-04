package com.example.personaldocumentproject.presentation.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaldocumentproject.R
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
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
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
                actions = {
                    ApplyButton(onClick = viewModel::applySetting)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // General Section
            item {
                SectionHeader(
                    title = "General",
                    isExpanded = state.isGeneralExpanded,
                    onToggleExpand = viewModel::toggleGeneralExpanded
                )

                AnimatedVisibility(visible = state.isGeneralExpanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        ToggleSettingItem(
                            label = "Dark mode",
                            checked = state.isDarkMode,
                            onCheckedChange = {
                                viewModel.setDarkMode(it)
                                viewModel.applySetting()
                            },
                        )
                    }
                }
            }

            // Section Divider
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = AppBorder,
                )
            }

            // Document Section
            item {
                SectionHeader(
                    title = "Document",
                    isExpanded = state.isDocumentExpanded,
                    onToggleExpand = viewModel::toggleDocumentExpanded
                )

                AnimatedVisibility(visible = state.isDocumentExpanded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        ToggleSettingItem(
                            label = "Show Image",
                            checked = state.showImage,
                            onCheckedChange = viewModel::setShowImage,
                        )

                        // Sliders Card Container
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceRaised)
                                .border(1.dp, AppBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SliderSettingRow(
                                label = "Width",
                                value = state.widthValue,
                                onValueChange = viewModel::setWidthValue,
                            )

                            SliderSettingRow(
                                label = "Height",
                                value = state.heightValue,
                                onValueChange = viewModel::setHeightValue,
                            )
                        }
                    }
                }
            }

            // Dynamic Preview Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Preview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(true) {
                    Box(
                        modifier = Modifier
                            .size(width = (40 + (state.widthValue * 140)).dp, (40 + (state.heightValue * 140)).dp)
                            .height(180.dp * (0.5f + state.heightValue * 0.5f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceRaised)
                            .border(1.dp, AppBorder, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.showImage) {
                            Image(
                                painter = painterResource(id = R.drawable.chihuahuaandroidtest),
                                contentDescription = "Document Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(Modifier.padding(16.dp)) {
                                Text("Title", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Document preview text", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Helper Composables ---

@Composable
private fun SectionHeader(
    title: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        label = "ChevronRotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Expand Section",
            modifier = Modifier
                .size(28.dp)
                .rotate(rotationAngle),
            tint = TextMuted,
        )
    }
}

@Composable
private fun ToggleSettingItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceRaised)
            .border(1.dp, AppBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = AppPrimary,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = AppBorder,
                uncheckedThumbColor = TextMuted,
            )
        )
    }
}

@Composable
private fun SliderSettingRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(60.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                activeTrackColor = AppPrimary,
                thumbColor = AppPrimary,
                inactiveTrackColor = AppBorder,
            )
        )
    }
}

@Composable
fun ApplyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppPrimary,
            contentColor = Color.White,
        )
    ) {
        Text(
            text = "Apply",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}