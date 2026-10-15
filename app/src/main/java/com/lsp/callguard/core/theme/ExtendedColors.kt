package com.lsp.callguard.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Roles Material 3 has no slot for: allowed calls (success) and notices (warning). */
@Immutable
data class CallGuardExtendedColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color
)

internal fun PaletteColors.toExtendedColors() = CallGuardExtendedColors(
    success = Color(success),
    successContainer = Color(successContainer),
    onSuccessContainer = Color(onSuccessContainer),
    warning = Color(warning),
    warningContainer = Color(warningContainer),
    onWarningContainer = Color(onWarningContainer)
)

internal val LocalCallGuardExtendedColors = staticCompositionLocalOf {
    CallGuardPalette.Light.toExtendedColors()
}

object CallGuardTheme {
    val extendedColors: CallGuardExtendedColors
        @Composable get() = LocalCallGuardExtendedColors.current
}
