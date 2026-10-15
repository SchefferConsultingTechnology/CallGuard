package com.lsp.callguard

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lsp.callguard.core.language.LocaleManagerHelper
import com.lsp.callguard.core.theme.CallGuardTheme
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.OnboardingPreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import com.lsp.callguard.ui.navigation.LegacyRead
import com.lsp.callguard.ui.navigation.LegacySignals
import com.lsp.callguard.ui.navigation.readLegacySignals
import com.lsp.callguard.ui.navigation.resolveStartDestination
import com.lsp.callguard.ui.navigation.shouldPersistDerivation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// AppCompatActivity (not ComponentActivity) is required for per-app language; see LocaleManagerHelper.
class MainActivity : AppCompatActivity() {

    private val languagePreferences by lazy {
        LanguagePreferences(applicationContext.appPreferencesDataStore)
    }

    private val onboardingPreferences by lazy {
        OnboardingPreferences(applicationContext.appPreferencesDataStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            LocaleManagerHelper.restoreIfMissing(languagePreferences.selectedLanguageFlow.first())

            val startDestination = resolveStartRoute()

            setContent {
                CallGuardTheme {
                    CallGuardApp(
                        languagePreferences = languagePreferences,
                        onboardingPreferences = onboardingPreferences,
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    /** Decided before composing so no first-run screen ever flashes for returning users. */
    private suspend fun resolveStartRoute(): String {
        val progress = runCatching { onboardingPreferences.progress() }.getOrNull()
        val legacy = if (progress == null) readLegacy() else null

        val resolution = resolveStartDestination(
            progress = progress,
            legacy = legacy?.signals ?: LegacySignals(
                hasSavedLanguage = false,
                hasWhitelistEntries = false,
                isLicensed = false
            )
        )

        if (shouldPersistDerivation(progressWasRead = progress != null, legacy = legacy)) {
            resolution.progressToPersist?.let { derived ->
                runCatching { onboardingPreferences.advanceTo(derived) }
            }
        }

        return resolution.route
    }

    private suspend fun readLegacy(): LegacyRead {
        return readLegacySignals(
            hasSavedLanguage = { languagePreferences.selectedLanguageFlow.first() != null },
            hasWhitelistEntries = {
                CallGuardDatabase.getInstance(applicationContext)
                    .allowedNumberDao().observeCount().first() > 0
            },
            isLicensed = {
                LicensePreferences(applicationContext.appPreferencesDataStore)
                    .licenseFlow.first().isLicensed
            }
        )
    }
}
