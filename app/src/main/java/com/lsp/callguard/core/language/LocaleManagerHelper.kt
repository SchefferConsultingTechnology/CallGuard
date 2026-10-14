package com.lsp.callguard.core.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Per-app language through AppCompat.
 *
 * AppCompatDelegate only reaches the platform LocaleManager (API 33+) through a live
 * AppCompatDelegate, and on API < 33 only applies the locale to AppCompatActivity hosts. That is
 * why MainActivity must extend AppCompatActivity: with a plain ComponentActivity every call here
 * was silently ignored. AppCompat (API < 33, with `autoStoreLocales`) or the system (API 33+)
 * persists the choice and recreates the activity.
 */
object LocaleManagerHelper {

    fun applyLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.code)
        )
    }

    /** Language currently applied to the app, or null when it follows the system. */
    fun currentLanguage(): AppLanguage? {
        val applied = AppCompatDelegate.getApplicationLocales()
        if (applied.isEmpty) return null

        return AppLanguage.fromCode(applied[0]?.toLanguageTag())
    }

    /**
     * Re-applies the language saved by the app when no per-app locale is set yet, e.g. for
     * installs that picked a language while the selection was being ignored.
     */
    fun restoreIfMissing(saved: AppLanguage?) {
        if (saved != null && AppCompatDelegate.getApplicationLocales().isEmpty) {
            applyLanguage(saved)
        }
    }
}
