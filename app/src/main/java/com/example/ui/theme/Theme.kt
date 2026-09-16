package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ChaiColorScheme = darkColorScheme(
    primary = ChaiRed,
    onPrimary = Color.White,
    primaryContainer = ChaiRedDark,
    onPrimaryContainer = Color.White,
    secondary = ChaiPurple,
    onSecondary = Color.White,
    secondaryContainer = ChaiPurpleDark,
    onSecondaryContainer = Color.White,
    background = ChaiBlack,
    onBackground = ChaiTextPrimary,
    surface = ChaiDarkSurface,
    onSurface = ChaiTextPrimary,
    surfaceVariant = ChaiCardBackground,
    onSurfaceVariant = ChaiTextSecondary,
    outline = ChaiBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChaiColorScheme,
        typography = Typography,
        content = content
    )
}
