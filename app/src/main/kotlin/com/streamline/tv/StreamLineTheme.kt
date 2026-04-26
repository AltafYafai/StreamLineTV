package com.streamline.tv

import androidx.compose.runtime.Composable
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StreamLineTheme(
    colors: StreamLineColors,
    content: @Composable () -> Unit
) {
    val tvColorScheme = darkColorScheme(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        surface = colors.surface,
        onSurface = colors.onSurface,
        surfaceVariant = colors.surfaceVariant,
        onSurfaceVariant = colors.onSurfaceVariant,
        border = colors.border
    )

    MaterialTheme(
        colorScheme = tvColorScheme,
        content = content
    )
}
