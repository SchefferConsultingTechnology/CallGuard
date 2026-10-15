package com.lsp.callguard

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.lsp.callguard.core.language.LocaleManagerHelper
import com.lsp.callguard.core.theme.CallGuardTheme
import com.lsp.callguard.core.theme.ThemeModeApplier
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.local.preferences.AppearancePreferences
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.OnboardingPreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import com.lsp.callguard.domain.model.ThemeMode
import com.lsp.callguard.domain.model.Tier
import com.lsp.callguard.ui.navigation.LegacyRead
import com.lsp.callguard.ui.navigation.LegacySignals
import com.lsp.callguard.ui.navigation.readLegacySignals
import com.lsp.callguard.ui.navigation.resolveStartDestination
import com.lsp.callguard.ui.navigation.shouldPersistDerivation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

// AppCompatActivity (not ComponentActivity) is required for per-app language; see LocaleManagerHelper.
class MainActivity : AppCompatActivity() {

    private val languagePreferences by lazy {
        LanguagePreferences(applicationContext.appPreferencesDataStore)
    }

    private val onboardingPreferences by lazy {
        OnboardingPreferences(applicationContext.appPreferencesDataStore)
    }

    private val appearancePreferences by lazy {
        AppearancePreferences(applicationContext.appPreferencesDataStore)
    }

    /** Null until the first screen is decided; the splash screen stays up until then. */
    @Volatile
    private var startDestination: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Before super.onCreate: swaps Theme.CallGuard.Starting for Theme.CallGuard (spec 006).
        installSplashScreen().setKeepOnScreenCondition { startDestination == null }

        // Theme is read here and never in CallGuardApplication: the call screening service also
        // starts the process, and nothing may delay the call response (constitution I).
        if (savedInstanceState == null && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // Below Android 12 the night mode must be set before the first frame, or the
            // screen is recreated right after opening. Android 12+ keeps it in the system.
            ThemeModeApplier.apply(this, readThemeModeQuickly())
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            LocaleManagerHelper.restoreIfMissing(languagePreferences.selectedLanguageFlow.first())

            val route = resolveStartRoute()

            setContent {
                CallGuardTheme {
                    CallGuardApp(
                        languagePreferences = languagePreferences,
                        onboardingPreferences = onboardingPreferences,
                        startDestination = route
                    )
                }
            }
            startDestination = route
        }
    }

    private fun readThemeModeQuickly(): ThemeMode = runBlocking {
        withTimeoutOrNull(THEME_READ_TIMEOUT_MS) {
            runCatching { appearancePreferences.themeModeFlow.first() }.getOrNull()
        } ?: ThemeMode.SYSTEM
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
                    .licenseFlow.first().tier != Tier.FREE
            }
        )
    }

    private companion object {
        const val THEME_READ_TIMEOUT_MS = 300L
    }
}
