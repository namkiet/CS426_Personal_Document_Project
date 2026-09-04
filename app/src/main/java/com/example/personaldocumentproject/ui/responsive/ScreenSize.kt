package com.example.personaldocumentproject.ui.responsive

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ScreenBreakpoint {
    COMPACT,
    MEDIUM,
    EXPANDED,
}

data class ScreenSizeInfo(
    val breakpoint: ScreenBreakpoint,
    val maxWidth: Dp,
    val maxHeight: Dp,
) {
    val isCompact: Boolean
        get() = breakpoint == ScreenBreakpoint.COMPACT

    val isWide: Boolean
        get() = breakpoint != ScreenBreakpoint.COMPACT
}

fun BoxWithConstraintsScope.screenSizeInfo(): ScreenSizeInfo {
    return when {
        maxWidth < 600.dp -> ScreenSizeInfo(ScreenBreakpoint.COMPACT, maxWidth, maxHeight)
        maxWidth < 900.dp -> ScreenSizeInfo(ScreenBreakpoint.MEDIUM, maxWidth, maxHeight)
        else -> ScreenSizeInfo(ScreenBreakpoint.EXPANDED, maxWidth, maxHeight)
    }
}
