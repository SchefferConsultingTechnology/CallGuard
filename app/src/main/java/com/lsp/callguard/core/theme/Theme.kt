package com.lsp.callguard.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = SurfaceLight,

    primaryContainer = BluePrimaryLight,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,

    outlineVariant = OutlineLight
)

@Composable
fun CallGuardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}