package com.kuzeykapisi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KuzeyColorScheme = lightColorScheme(
    primary = Deniz,
    onPrimary = Kagit2,
    secondary = Petrol,
    onSecondary = Kagit2,
    tertiary = Pirinc,
    onTertiary = Kagit2,
    background = Kagit,
    onBackground = Murekkep,
    surface = Kagit2,
    onSurface = Murekkep,
    surfaceVariant = Kagit,
    onSurfaceVariant = Sur,
)

@Composable
fun KuzeyKapisiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KuzeyColorScheme,
        typography = KuzeyTypography,
        content = content,
    )
}
