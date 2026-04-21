package com.lsp.callguard.core.language

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleManagerHelper {

    fun applyLanguage(context: Context, language: AppLanguage) {
        val locale = when (language) {
            AppLanguage.PT_BR -> Locale("pt", "BR")
            AppLanguage.EN_US -> Locale("en", "US")
            AppLanguage.ES_ES -> Locale("es", "ES")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager.applicationLocales = LocaleList(locale)
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.create(locale)
            )
        }
    }
}