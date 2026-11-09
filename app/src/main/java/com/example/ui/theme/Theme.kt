package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ArchiveAccentNeutral,
    onPrimary = ArchiveBackground,
    primaryContainer = ArchiveSurfaceElevated,
    onPrimaryContainer = ArchiveTextPrimary,
    secondary = ArchiveTextSecondary,
    onSecondary = ArchiveBackground,
    background = ArchiveBackground,
    onBackground = ArchiveTextPrimary,
    surface = ArchiveSurface,
    onSurface = ArchiveTextPrimary,
    surfaceVariant = ArchiveSurfaceElevated,
    onSurfaceVariant = ArchiveTextSecondary,
    outline = ArchiveSurfaceBorder,
    outlineVariant = ArchiveDivider,
    error = ArchiveHeroRed,
    onError = ArchiveTextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // The application is strictly dark, quiet, and editorial by design
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
