package com.example.personaldocumentproject.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ── Dark Theme Color Tokens ──────────────────────────────────────────────────
val DarkAppBackground = Color(0xFF0D0D12)
val DarkAppSurface = Color(0xFF16161E)
val DarkSurfaceRaised = Color(0xFF1E1E2A)
val DarkAppBorder = Color(0xFF2A2A38)
val DarkAppPrimary = Color(0xFF7C6FFF)
val DarkAppAccent = Color(0xFFFF6B8A)
val DarkTextPrimary = Color(0xFFF0EFF8)
val DarkTextMuted = Color(0xFF8A8A9F)
val DarkTextDim = Color(0xFF55556A)
val DarkDeleteColor = Color(0xFFFF6675)

val DarkStudyColor = Color(0xFF8B9FFF)
val DarkReceiptColor = Color(0xFFFFB066)
val DarkNoteColor = Color(0xFF66FFB2)
val DarkIdentityColor = Color(0xFF4DD0E1)
val DarkWorkColor = Color(0xFFBA68C8)

// ── Light Theme Color Tokens ─────────────────────────────────────────────────
val LightAppBackground = Color(0xFFF6F7FB)
val LightAppSurface = Color(0xFFFFFFFF)
val LightSurfaceRaised = Color(0xFFEDEFF5)
val LightAppBorder = Color(0xFFE2E4EB)
val LightAppPrimary = Color(0xFF6352E8)
val LightAppAccent = Color(0xFFFF5277)
val LightTextPrimary = Color(0xFF161622)
val LightTextMuted = Color(0xFF636578)
val LightTextDim = Color(0xFF9094A6)
val LightDeleteColor = Color(0xFFE53935)

val LightStudyColor = Color(0xFF3F51B5)
val LightReceiptColor = Color(0xFFD97706)
val LightNoteColor = Color(0xFF0D9488)
val LightIdentityColor = Color(0xFF0284C7)
val LightWorkColor = Color(0xFF7C3AED)

// ── Dynamic Theme Accessors (auto-switch based on active theme) ───────────────
val AppBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val AppSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val SurfaceRaised: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val AppBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val AppPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

val AppAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiary

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextDim: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outlineVariant

val DeleteColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.error

val StudyColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background == DarkAppBackground) DarkStudyColor else LightStudyColor

val ReceiptColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background == DarkAppBackground) DarkReceiptColor else LightReceiptColor

val NoteColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background == DarkAppBackground) DarkNoteColor else LightNoteColor

val IdentityColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background == DarkAppBackground) DarkIdentityColor else LightIdentityColor

val WorkColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background == DarkAppBackground) DarkWorkColor else LightWorkColor
