package com.lsp.callguard.core.theme

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/** WCAG 2.x contrast for every pair in specs/006-visual-identity/contracts/color-roles.md. */
class CallGuardPaletteContrastTest {

    @Test
    fun `light palette text and controls are readable`() = assertReadable("light", CallGuardPalette.Light)

    @Test
    fun `dark palette text and controls are readable`() = assertReadable("dark", CallGuardPalette.Dark)

    private fun assertReadable(theme: String, p: PaletteColors) {
        val text = listOf(
            "onBackground/background" to (p.onBackground to p.background),
            "onSurface/surface" to (p.onSurface to p.surface),
            "onSurface/dialogSurface" to (p.onSurface to p.dialogSurface),
            "onSurface/surfaceVariant" to (p.onSurface to p.surfaceVariant),
            "onSurfaceVariant/background" to (p.onSurfaceVariant to p.background),
            "onSurfaceVariant/surface" to (p.onSurfaceVariant to p.surface),
            "onSurfaceVariant/dialogSurface" to (p.onSurfaceVariant to p.dialogSurface),
            "onSurfaceVariant/surfaceVariant" to (p.onSurfaceVariant to p.surfaceVariant),
            "primary/background" to (p.primary to p.background),
            "primary/surface" to (p.primary to p.surface),
            "primary/dialogSurface" to (p.primary to p.dialogSurface),
            "primary/surfaceVariant" to (p.primary to p.surfaceVariant),
            "onPrimary/primary" to (p.onPrimary to p.primary),
            "onPrimaryContainer/primaryContainer" to (p.onPrimaryContainer to p.primaryContainer),
            "onSecondaryContainer/secondaryContainer" to (p.onSecondaryContainer to p.secondaryContainer),
            "error/surface" to (p.error to p.surface),
            "success/surface" to (p.success to p.surface),
            "warning/surface" to (p.warning to p.surface),
            "onErrorContainer/errorContainer" to (p.onErrorContainer to p.errorContainer),
            "onSuccessContainer/successContainer" to (p.onSuccessContainer to p.successContainer),
            "onWarningContainer/warningContainer" to (p.onWarningContainer to p.warningContainer),
            "inverseOnSurface/inverseSurface" to (p.inverseOnSurface to p.inverseSurface)
        )
        val controls = listOf(
            "outline/surface" to (p.outline to p.surface),
            "primary/primaryContainer" to (p.primary to p.primaryContainer)
        )

        val failures = text.mapNotNull { (name, pair) -> failure(name, pair, 4.5) } +
            controls.mapNotNull { (name, pair) -> failure(name, pair, 3.0) }

        assertTrue("$theme theme below WCAG AA:\n" + failures.joinToString("\n"), failures.isEmpty())
    }

    private fun failure(name: String, pair: Pair<Long, Long>, minimum: Double): String? {
        val ratio = contrast(pair.first, pair.second)
        return if (ratio < minimum) "$name = ${"%.2f".format(ratio)} (min $minimum)" else null
    }

    private fun contrast(a: Long, b: Long): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private fun luminance(argb: Long): Double {
        fun channel(shift: Int): Double {
            val c = ((argb shr shift) and 0xFF) / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }
}
