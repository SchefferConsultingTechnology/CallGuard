package com.lsp.callguard.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = CallGuardPalette.Light.toColorScheme(dark = false)
private val DarkColors = CallGuardPalette.Dark.toColorScheme(dark = true)

private val LightExtended = CallGuardPalette.Light.toExtendedColors()
private val DarkExtended = CallGuardPalette.Dark.toExtendedColors()

/**
 * Every Material 3 role is filled from the brand palette so no component falls back to the default
 * purple. No dynamic (wallpaper) colours: the brand stays recognisable (spec FR-012).
 * [darkTheme] already reflects the person's choice: AppCompat/UiModeManager set the night mode.
 */
@Composable
fun CallGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCallGuardExtendedColors provides if (darkTheme) DarkExtended else LightExtended
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            content = content
        )
    }
}

private fun PaletteColors.toColorScheme(dark: Boolean): ColorScheme {
    val flat = Color(surface)
    val base = if (dark) darkColorScheme() else lightColorScheme()

    return base.copy(
        primary = Color(primary),
        onPrimary = Color(onPrimary),
        primaryContainer = Color(primaryContainer),
        onPrimaryContainer = Color(onPrimaryContainer),
        inversePrimary = Color(if (dark) CallGuardPalette.BLUE else CallGuardPalette.Dark.primary),
        // Secondary follows primary; the tonal button uses secondaryContainer.
        secondary = Color(primary),
        onSecondary = Color(onPrimary),
        secondaryContainer = Color(secondaryContainer),
        onSecondaryContainer = Color(onSecondaryContainer),
        tertiary = Color(tertiary),
        onTertiary = Color(CallGuardPalette.NAVY),
        tertiaryContainer = Color(primaryContainer),
        onTertiaryContainer = Color(onPrimaryContainer),
        background = Color(background),
        onBackground = Color(onBackground),
        surface = flat,
        onSurface = Color(onSurface),
        surfaceVariant = Color(surfaceVariant),
        onSurfaceVariant = Color(onSurfaceVariant),
        // Cards stay flat: no tonal tint over surfaces.
        surfaceTint = flat,
        inverseSurface = Color(inverseSurface),
        inverseOnSurface = Color(inverseOnSurface),
        error = Color(error),
        onError = Color(onError),
        errorContainer = Color(errorContainer),
        onErrorContainer = Color(onErrorContainer),
        outline = Color(outline),
        outlineVariant = Color(outlineVariant),
        scrim = Color.Black,
        surfaceBright = flat,
        surfaceDim = Color(background),
        surfaceContainerLowest = flat,
        surfaceContainerLow = flat,
        surfaceContainer = flat,
        // Dialogs, menus and bottom sheets.
        surfaceContainerHigh = Color(dialogSurface),
        surfaceContainerHighest = Color(surfaceVariant)
    )
}
