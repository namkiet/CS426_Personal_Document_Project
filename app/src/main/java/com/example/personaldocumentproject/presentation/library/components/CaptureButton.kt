package com.example.personaldocumentproject.presentation.library.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp

@Composable
fun CaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

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
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. expanding big circle overlay
        if (animationProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Button center coordinates
                        val centerX = size.width / 2f
                        val centerY = size.height - 52.dp.toPx()

                        // Target radius calculation relative to screen size
                        val maxRadius = size.width * 0.85f
                        val currentRadius = maxRadius * animationProgress

                        // Draw single expanding background circle centered on the button
                        drawCircle(
                            color = overlayColor,
                            radius = currentRadius,
                            center = Offset(centerX, centerY)
                        )

                        // Draw vertical dashed divider line through the center
                        val startY = centerY - currentRadius
                        val endY = centerY + currentRadius

                        drawLine(
                            color = dividerColor,
                            start = Offset(centerX, startY),
                            end = Offset(centerX, endY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(12f, 8f),
                                phase = 0f
                            )
                        )
                    }
            )
        }

        // 2. Main Circle Button
        Box(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(84.dp)
                .clip(CircleShape)
                .drawBehind { drawCircle(buttonColor) }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isExpanded = !isExpanded
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

