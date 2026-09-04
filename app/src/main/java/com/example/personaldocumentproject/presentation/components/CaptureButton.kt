package com.example.personaldocumentproject.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.ui.imageVector.*


@Composable
fun CaptureButton(
    onLeftHalfClick: () -> Unit,
    onRightHalfClick: () -> Unit,
    onClick: () -> Unit,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val buttonBottomPadding = if(!isLandscape) 52.dp else 0.dp
    val buttonEndPadding = if(isLandscape) 52.dp else 0.dp
    val centerFromBottom = buttonBottomPadding + 52.dp
    val centerFromEnd = buttonEndPadding + 52.dp
    val iconSize = 84.dp

    val leftHalfButtonBias = BiasAlignment(
        horizontalBias = if(!isLandscape) 0.2f else -0.5f,
        verticalBias = if(!isLandscape) -0.5f else -0.2f,
    )
    val rightHalfButtonBias = BiasAlignment(
        horizontalBias = if(!isLandscape) -0.2f else -0.5f,
        verticalBias = if(!isLandscape) -0.5f else 0.2f,
    )

    // Smoothly animate transition progress (0f = collapsed, 1f = expanded)
    val animationProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "ExpandAnimation"
    )

    // Smoothly rotate '+' to 'x' (45 degrees)
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "RotationAnimation"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isExpanded) Color(0xFFD32F2F) else Color(0xFF2D5A1E),
        animationSpec = tween(durationMillis = 300),
        label = "ColorAnimation"
    )

    val overlayColor = Color(0xFF2D5A1E).copy(alpha = 0.35f)
    val dividerColor = Color(0xFF1B3813)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = if (!isLandscape) Alignment.BottomCenter else Alignment.CenterEnd
    ) {
        // 1. expanding big circle overlay
        if (animationProgress > 0f) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = if (!isLandscape) Alignment.BottomCenter else Alignment.CenterEnd
            ) {
                // Target radius calculation relative to screen size (uses min dimension in landscape)
                val baseDimension = if (isLandscape) configuration.screenHeightDp.dp else configuration.screenWidthDp.dp
                val maxRadius = baseDimension * 0.55f
                val currentRadius = maxRadius * animationProgress
                val currentSize = currentRadius * 2

                Box(
                    modifier = Modifier
                        .offset(y = if(!isLandscape) -centerFromBottom + currentRadius else 0.dp,
                            x = if(isLandscape) -centerFromEnd + currentRadius else 0.dp
                        )
                        .wrapContentSize(unbounded = true)
                        .size(width = currentSize, height = currentSize)
                        .clip(CircleShape)
                ) {

                    if (!isLandscape){
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left Half Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(overlayColor)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true)
                                    ) {
                                        onLeftHalfClick()
                                    },
                                contentAlignment = leftHalfButtonBias
                            )
                            {
                                Icon(
                                    imageVector = FluentuiSystemIconsCameraAddSolid,
                                    contentDescription = "CameraHalf",
                                    modifier = Modifier.size(iconSize),
                                    tint = Color.Black
                                )
                            }

                            // Right Half Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(overlayColor)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true)
                                    ) {
                                        onRightHalfClick()
                                    },
                                contentAlignment = rightHalfButtonBias
                            )
                            {
                                Icon(
                                    imageVector = FluentuiSystemIconsAlbumAddSolid,
                                    contentDescription = "ImportHalf",
                                    modifier = Modifier.size(iconSize),
                                    tint = Color.Black
                                )
                            }
                        }
                    }else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Right Half Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(overlayColor)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true)
                                    ) {
                                        onRightHalfClick()
                                    },
                                contentAlignment = rightHalfButtonBias
                            )
                            {
                                Icon(
                                    imageVector = FluentuiSystemIconsAlbumAddSolid,
                                    contentDescription = "ImportHalf",
                                    modifier = Modifier.size(iconSize),
                                    tint = Color.Black
                                )
                            }
                            // Left Half Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(overlayColor)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true)
                                    ) {
                                        onLeftHalfClick()
                                    },
                                contentAlignment = leftHalfButtonBias
                            )
                            {
                                Icon(
                                    imageVector = FluentuiSystemIconsCameraAddSolid,
                                    contentDescription = "CameraHalf",
                                    modifier = Modifier.size(iconSize),
                                    tint = Color.Black
                                )
                            }
                        }
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2f
                        val centerY = size.height /2f
                        drawLine(
                            color = dividerColor,
                            start = if (!isLandscape) Offset(centerX, 0f) else Offset(size.width, centerY),
                            end = if (!isLandscape) Offset(centerX, size.height) else Offset(0f, centerY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(12f, 8f),
                                phase = 0f
                            )
                        )
                    }
                }
            }
        }

        // 2. Main Circle Button
        Box(
            modifier = Modifier
                .padding(bottom = buttonBottomPadding, end = buttonEndPadding)
                .size(84.dp)
                .clip(CircleShape)
                .drawBehind { drawCircle(buttonColor) }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Expand Button",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .rotate(rotationAngle)
            )
        }
    }
}

