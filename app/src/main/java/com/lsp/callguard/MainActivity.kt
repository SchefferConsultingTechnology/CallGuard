package com.lsp.callguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.lsp.callguard.core.language.LocaleManagerHelper
import com.lsp.callguard.core.theme.CallGuardTheme
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val languagePreferences by lazy {
        LanguagePreferences(applicationContext.appPreferencesDataStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val savedLanguage = languagePreferences.selectedLanguageFlow.first()

            if (savedLanguage != null) {
                LocaleManagerHelper.applyLanguage(savedLanguage)
            }

            setContent {
                CallGuardTheme {
                    CallGuardApp(
                        languagePreferences = languagePreferences
                    )
                }
            }
        }
    }
}