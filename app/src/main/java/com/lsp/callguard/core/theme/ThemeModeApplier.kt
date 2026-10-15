package com.lsp.callguard.core.theme

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.lsp.callguard.domain.model.ThemeMode

/**
 * Applies the theme choice to the whole app (Compose, XML resources, system bars, splash).
 * Android 12+: UiModeManager only — the system keeps it and uses it for the splash screen.
 * Older versions: AppCompat only. Calling both would recreate the screen twice (research R4).
 */
object ThemeModeApplier {

    fun apply(context: Context, mode: ThemeMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiModeManager = context.getSystemService(UiModeManager::class.java) ?: return
            val target = when (mode) {
                ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
                ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
                ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
            }
            // Re-applying the current mode does not change the configuration, so no recreate.
            uiModeManager.setApplicationNightMode(target)
        } else {
            val target = when (mode) {
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            }
            // AppCompat ignores a mode equal to the current one.
            AppCompatDelegate.setDefaultNightMode(target)
        }
    }
}
