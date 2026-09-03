package com.example.personaldocumentproject.presentation.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.personaldocumentproject.R
import com.example.personaldocumentproject.ui.imageVector.FluentuiSystemIconsPerson
import com.example.personaldocumentproject.ui.imageVector.FluentuiSystemIconsSettings

@Composable
fun SettingRoute(viewModel: SettingViewModel,
                 onBack: () -> Unit) {
    SettingsScreen(
        viewModel = viewModel,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // --- Color Scheme Setup ---
    val backgroundColor = MaterialTheme.colorScheme.secondary
    val itemBackgroundColor = Color.White
    val accentColor = MaterialTheme.colorScheme.tertiary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search",
                        //style = MaterialTheme.typography.bodyLarge,
                    )
                },
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
                    ApplyButton(
                        onClick = viewModel::applySetting
                    )
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                //.background(backgroundColor)
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Title
            item {
                Text(
                    text = "Setting",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

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
                            onCheckedChange = viewModel::setDarkMode,
                            containerColor = itemBackgroundColor,
                            activeColor = accentColor
                        )
                    }
                }
            }

            // Section Divider
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.6f)
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
                            containerColor = itemBackgroundColor,
                            activeColor = accentColor
                        )

                        // Sliders Card Container
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(itemBackgroundColor)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SliderSettingRow(
                                label = "Width",
                                value = state.widthValue,
                                onValueChange = viewModel::setWidthValue,
                                activeColor = accentColor
                            )

                            SliderSettingRow(
                                label = "Height",
                                value = state.heightValue,
                                onValueChange = viewModel::setHeightValue,
                                activeColor = accentColor
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
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Reactive Image Placeholder sizing driven by Sliders & Toggle
                AnimatedVisibility(true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f * (0.5f + state.widthValue * 0.5f))
                            .height(180.dp * (0.5f + state.heightValue * 0.5f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(accentColor)
                    ) {
                        if (state.showImage) {
                            Column() {
                                Image(
                                    painter = painterResource(id = R.drawable.chihuahuaandroidtest),
                                    contentDescription = "Document Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                        } else {
                            Column(Modifier.padding(16.dp)) {
                                Text("Title", style = MaterialTheme.typography.titleMedium)
                                Text("Lorem ipsum", style = MaterialTheme.typography.bodyMedium)
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
            fontSize = 22.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Expand Section",
            modifier = Modifier
                .size(36.dp)
                .rotate(rotationAngle),
            tint = Color.Black
        )
    }
}

@Composable
private fun ToggleSettingItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    containerColor: Color,
    activeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = activeColor,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray,
                uncheckedThumbColor = Color.White
            )
        )
    }
}

@Composable
private fun SliderSettingRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    activeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.width(72.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                activeTrackColor = activeColor,
                thumbColor = Color.White,
                inactiveTrackColor = activeColor.copy(alpha = 0.4f)
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
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
    ) {
        Text(
            text = "Apply",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
    }
}