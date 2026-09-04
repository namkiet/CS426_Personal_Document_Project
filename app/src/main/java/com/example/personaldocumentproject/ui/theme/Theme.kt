package com.example.personaldocumentproject.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkAppPrimary,
    onPrimary = DarkTextPrimary,
    background = DarkAppBackground,
    onBackground = DarkTextPrimary,
    surface = DarkAppSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceRaised,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkAppBorder,
    outlineVariant = DarkTextDim,
    error = DarkDeleteColor,
    tertiary = DarkAppAccent,
)

private val LightColorScheme = lightColorScheme(
    primary = LightAppPrimary,
    onPrimary = Color.White,
    background = LightAppBackground,
    onBackground = LightTextPrimary,
    surface = LightAppSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceRaised,
    onSurfaceVariant = LightTextMuted,
    outline = LightAppBorder,
    outlineVariant = LightTextDim,
    error = LightDeleteColor,
    tertiary = LightAppAccent,
)

@Composable
fun PersonalDocumentProjectTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
