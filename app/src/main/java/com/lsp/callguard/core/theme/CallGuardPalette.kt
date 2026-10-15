package com.lsp.callguard.core.theme

/**
 * Brand colour roles as plain ARGB values (no Compose), so the contrast test runs on the JVM.
 * Values and rationale: specs/006-visual-identity/research.md § R2.
 */
data class PaletteColors(
    val background: Long,
    val surface: Long,
    val surfaceVariant: Long,
    val dialogSurface: Long,
    val outline: Long,
    val outlineVariant: Long,
    val onBackground: Long,
    val onSurface: Long,
    val onSurfaceVariant: Long,
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondaryContainer: Long,
    val onSecondaryContainer: Long,
    val tertiary: Long,
    val error: Long,
    val onError: Long,
    val errorContainer: Long,
    val onErrorContainer: Long,
    val success: Long,
    val successContainer: Long,
    val onSuccessContainer: Long,
    val warning: Long,
    val warningContainer: Long,
    val onWarningContainer: Long,
    val inverseSurface: Long,
    val inverseOnSurface: Long
)

object CallGuardPalette {

    const val NAVY = 0xFF0B1F3B
    const val BLUE = 0xFF2563EB
    const val CYAN = 0xFF22D3EE

    val Light = PaletteColors(
        background = 0xFFF5F7FB,
        surface = 0xFFFFFFFF,
        surfaceVariant = 0xFFEEF2F8,
        dialogSurface = 0xFFFFFFFF,
        outline = 0xFF7A879C,
        outlineVariant = 0xFFDCE3EE,
        onBackground = NAVY,
        onSurface = NAVY,
        onSurfaceVariant = 0xFF4A5A75,
        primary = BLUE,
        onPrimary = 0xFFFFFFFF,
        primaryContainer = 0xFFDBE6FE,
        onPrimaryContainer = NAVY,
        secondaryContainer = 0xFFDBE6FE,
        onSecondaryContainer = NAVY,
        tertiary = CYAN,
        error = 0xFFDC2626,
        onError = 0xFFFFFFFF,
        errorContainer = 0xFFFEE2E2,
        onErrorContainer = 0xFF991B1B,
        success = 0xFF15803D,
        successContainer = 0xFFDCFCE7,
        onSuccessContainer = 0xFF14532D,
        warning = 0xFFB45309,
        warningContainer = 0xFFFEF3C7,
        onWarningContainer = 0xFF78350F,
        inverseSurface = NAVY,
        inverseOnSurface = 0xFFFFFFFF
    )

    // Brand blue is too dark on navy surfaces (2.9:1), so dark mode uses a lighter blue with navy text.
    val Dark = PaletteColors(
        background = 0xFF071426,
        surface = 0xFF0F2747,
        surfaceVariant = 0xFF16304F,
        dialogSurface = 0xFF132C4F,
        outline = 0xFF6F84A3,
        outlineVariant = 0xFF1F3A5F,
        onBackground = 0xFFE6EDF7,
        onSurface = 0xFFE6EDF7,
        onSurfaceVariant = 0xFF9DB0CC,
        primary = 0xFF5B9BFA,
        onPrimary = NAVY,
        primaryContainer = 0xFF1E3A66,
        onPrimaryContainer = 0xFFDBE6FE,
        secondaryContainer = 0xFF1E3A66,
        onSecondaryContainer = 0xFFDBE6FE,
        tertiary = CYAN,
        error = 0xFFF87171,
        onError = NAVY,
        errorContainer = 0xFF3B1219,
        onErrorContainer = 0xFFFECACA,
        success = 0xFF4ADE80,
        successContainer = 0xFF0E2E1D,
        onSuccessContainer = 0xFFBBF7D0,
        warning = 0xFFFBBF24,
        warningContainer = 0xFF33260A,
        onWarningContainer = 0xFFFDE68A,
        inverseSurface = 0xFFE6EDF7,
        inverseOnSurface = NAVY
    )
}
