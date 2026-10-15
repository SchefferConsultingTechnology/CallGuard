package com.lsp.callguard

import androidx.compose.runtime.Composable
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.OnboardingPreferences
import com.lsp.callguard.ui.navigation.CallGuardNavHost

@Composable
fun CallGuardApp(
    languagePreferences: LanguagePreferences,
    onboardingPreferences: OnboardingPreferences,
    startDestination: String
) {
    CallGuardNavHost(
        languagePreferences = languagePreferences,
        onboardingPreferences = onboardingPreferences,
        startDestination = startDestination
    )
}
