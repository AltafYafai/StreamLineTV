package com.streamline.tv

import androidx.compose.ui.graphics.Color

data class StreamLineColors(
    val primary: Color,
    val onPrimary: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val border: Color
)

object ThemePresets {
    val DeepPurple = StreamLineColors(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        border = Color(0xFF938F99)
    )

    val OrganicGreen = StreamLineColors(
        primary = Color(0xFFB4D2A7),
        onPrimary = Color(0xFF20361A),
        surface = Color(0xFF1A1C19),
        onSurface = Color(0xFFE2E3DD),
        surfaceVariant = Color(0xFF424940),
        onSurfaceVariant = Color(0xFFC1C9BE),
        border = Color(0xFF8B9389)
    )

    val OceanBlue = StreamLineColors(
        primary = Color(0xFFA8C8FF),
        onPrimary = Color(0xFF003062),
        surface = Color(0xFF1A1C1E),
        onSurface = Color(0xFFE2E2E6),
        surfaceVariant = Color(0xFF43474E),
        onSurfaceVariant = Color(0xFFC3C7CF),
        border = Color(0xFF8D9199)
    )
}
