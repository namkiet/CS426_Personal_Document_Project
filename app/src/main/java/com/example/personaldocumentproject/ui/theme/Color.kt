package com.example.personaldocumentproject.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ── Dark Theme Color Tokens ──────────────────────────────────────────────────
val DarkAppBackground = Color(0xFF0D0D12)
val DarkAppSurface = Color(0xFF161620)
val DarkSurfaceRaised = Color(0xFF1F202E)
val DarkAppBorder = Color(0xFF2B2C3D)
val DarkAppPrimary = Color(0xFF7C6FFF)
val DarkAppAccent = Color(0xFFFF6489)
val DarkTextPrimary = Color(0xFFF3F4F8)
val DarkTextMuted = Color(0xFF9092A6)
val DarkTextDim = Color(0xFF5C5E73)
val DarkDeleteColor = Color(0xFFFF5C6C)

val DarkStudyColor = Color(0xFF8B9FFF)
val DarkReceiptColor = Color(0xFFFFB066)
val DarkNoteColor = Color(0xFF5CE4A8)
val DarkIdentityColor = Color(0xFF4DD0E1)
val DarkWorkColor = Color(0xFFC084FC)

// ── Light Theme Color Tokens ─────────────────────────────────────────────────
val LightAppBackground = Color(0xFFF6F7FB)
val LightAppSurface = Color(0xFFFFFFFF)
val LightSurfaceRaised = Color(0xFFECEEF5)
val LightAppBorder = Color(0xFFE2E4ED)
val LightAppPrimary = Color(0xFF5B4DF2)
val LightAppAccent = Color(0xFFF43F5E)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextMuted = Color(0xFF475569)
val LightTextDim = Color(0xFF94A3B8)
val LightDeleteColor = Color(0xFFE11D48)

val LightStudyColor = Color(0xFF3B54DF)
val LightReceiptColor = Color(0xFFD97706)
val LightNoteColor = Color(0xFF059669)
val LightIdentityColor = Color(0xFF0284C7)
val LightWorkColor = Color(0xFF7E22CE)

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
