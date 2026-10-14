package com.lsp.callguard

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lsp.callguard.core.language.LocaleManagerHelper
import com.lsp.callguard.core.theme.CallGuardTheme
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// AppCompatActivity (not ComponentActivity) is required for per-app language; see LocaleManagerHelper.
class MainActivity : AppCompatActivity() {

    private val languagePreferences by lazy {
        LanguagePreferences(applicationContext.appPreferencesDataStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            LocaleManagerHelper.restoreIfMissing(languagePreferences.selectedLanguageFlow.first())

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
