package com.gym.manager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val GymColors = darkColorScheme(
    primary = GymPrimary,
    onPrimary = Color.White,
    secondary = GymPrimary,
    background = GymBg,
    onBackground = GymTextPrimary,
    surface = GymSurface,
    onSurface = GymTextPrimary,
    surfaceVariant = GymCard,
    onSurfaceVariant = GymTextSecondary,
    surfaceContainerLowest = GymBg,
    surfaceContainerLow = GymSurface,
    surfaceContainer = GymSurface,
    surfaceContainerHigh = GymCard,
    surfaceContainerHighest = GymCard,
    error = StatusExpired,
    outline = GymOutline,
    outlineVariant = GymOutline
)

/** The app is Arabic, so the layout is always right-to-left. */
@Composable
fun GymTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = GymColors, typography = GymTypography, content = content)
    }
}
