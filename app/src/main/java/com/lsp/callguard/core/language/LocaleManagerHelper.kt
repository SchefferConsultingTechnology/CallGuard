package com.lsp.callguard.core.language

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleManagerHelper {
    fun applyLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.code)
        )
    }

    /**
     * Applies the language and, on API < 33, immediately recreates the hosting Activity.
     *
     * MainActivity extends ComponentActivity, not AppCompatActivity, so on API < 33
     * AppCompatDelegate.setApplicationLocales() does not trigger the automatic
     * recreate() that AppCompat performs for AppCompatActivity hosts, and resource
     * strings would otherwise only refresh on the next app start.
     *
     * On API 33+, setApplicationLocales() delegates to the platform LocaleManager,
     * which recreates the foreground Activity itself as an async configuration
     * change. Calling recreate() manually there races that system-triggered
     * recreate: our recreate can fire before the platform has committed the new
     * locale, so it briefly redraws with the OLD strings, which is what made the
     * screen look like it wasn't reflecting the language on click.
     */
    fun applyLanguage(context: Context, language: AppLanguage) {
        applyLanguage(language)
        if (Build.VERSION.SDK_INT < 33) {
            context.findActivity()?.recreate()
        }
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}