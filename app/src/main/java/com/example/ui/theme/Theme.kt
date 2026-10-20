package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AlFajrColorScheme = darkColorScheme(
    primary = PrimaryGold,
    secondary = SecondaryGreen,
    tertiary = TertiaryLcd,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceContainer = SurfaceContainerDark,
    onPrimary = BackgroundDark,
    onSecondary = BackgroundDark,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AlFajrColorScheme,
        typography = Typography,
        content = content
    )
}
