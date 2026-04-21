package com.lsp.callguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lsp.callguard.core.theme.CallGuardTheme
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore

class MainActivity : ComponentActivity() {

    // 🔹 Instância única do LanguagePreferences
    private val languagePreferences by lazy {
        LanguagePreferences(applicationContext.appPreferencesDataStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CallGuardTheme {

                CallGuardApp(
                    languagePreferences = languagePreferences
                )

            }
        }
    }
}