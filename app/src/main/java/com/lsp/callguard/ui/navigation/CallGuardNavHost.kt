package com.lsp.callguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lsp.callguard.R
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.OnboardingPreferences
import com.lsp.callguard.domain.model.OnboardingProgress
import com.lsp.callguard.ui.screen.consent.ContactsConsentRoute
import com.lsp.callguard.ui.screen.consent.ContactsConsentScreen
import com.lsp.callguard.ui.screen.contacts.ContactsImportRoute
import com.lsp.callguard.ui.screen.history.CallHistoryRoute
import com.lsp.callguard.ui.screen.home.HomeRoute
import com.lsp.callguard.ui.screen.language.LanguageSelectionScreen
import com.lsp.callguard.ui.screen.language.rememberLanguageSelectionViewModel
import com.lsp.callguard.ui.screen.legal.LegalScreen
import com.lsp.callguard.ui.screen.onboarding.OnboardingScreen
import com.lsp.callguard.ui.screen.paywall.PaywallRoute
import com.lsp.callguard.ui.screen.protection.ProtectionSetupRoute
import com.lsp.callguard.ui.screen.settings.SettingsRoute
import com.lsp.callguard.ui.screen.whitelist.WhitelistRoute
import kotlinx.coroutines.launch

@Composable
fun CallGuardNavHost(
    languagePreferences: LanguagePreferences,
    onboardingPreferences: OnboardingPreferences,
    startDestination: String
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.ProtectionSetup.route) {
            ProtectionSetupRoute(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Onboarding.route) {
            OnboardingScreen(
                languagePreferences = languagePreferences,
                onContinue = {
                    scope.launch {
                        runCatching { onboardingPreferences.advanceTo(OnboardingProgress.ONBOARDING_DONE) }
                        // Onboarding stays below the offer so "back" returns to it.
                        navController.navigate(Routes.Paywall.create(firstRun = true)) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Routes.Language.route) {
            val viewModel = rememberLanguageSelectionViewModel(languagePreferences)
            val uiState by viewModel.uiState.collectAsState()

            LanguageSelectionScreen(
                selectedLanguage = uiState.selectedLanguage,
                onSelectLanguage = viewModel::onLanguageSelected,
                // Only reachable from Settings; never part of the first run.
                onDone = { navController.popBackStack() }
            )
        }

        composable(Routes.Home.route) {
            HomeRoute(
                onOpenWhitelist = {
                    navController.navigate(Routes.Whitelist.route)
                },
                onOpenSettings = {
                    navController.navigate(Routes.Settings.route)
                },
                onOpenPaywall = {
                    navController.navigate(Routes.Paywall.create(firstRun = false))
                },
                onOpenProtectionSetup = {
                    navController.navigate(Routes.ProtectionSetup.route)
                },
                onOpenCallHistory = {
                    navController.navigate(Routes.CallHistory.route)
                },
                onOpenContactsConsent = {
                    navController.navigate(Routes.ContactsConsent.route)
                }
            )
        }

        composable(Routes.Whitelist.route) {
            WhitelistRoute(
                onBack = {
                    navController.popBackStack()
                },
                onOpenPaywall = {
                    navController.navigate(Routes.Paywall.create(firstRun = false))
                }
            )
        }

        composable(Routes.Settings.route) {
            SettingsRoute(
                onBack = {
                    navController.popBackStack()
                },
                onOpenPaywall = {
                    navController.navigate(Routes.Paywall.create(firstRun = false))
                },
                onOpenLanguage = {
                    navController.navigate(Routes.Language.route)
                },
                onOpenTerms = {
                    navController.navigate(Routes.Terms.route)
                },
                onOpenPrivacyPolicy = {
                    navController.navigate(Routes.PrivacyPolicy.route)
                }
            )
        }

        composable(
            route = Routes.Paywall.route,
            arguments = listOf(
                navArgument(Routes.Paywall.ARG_FIRST_RUN) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val firstRun = backStackEntry.arguments?.getBoolean(Routes.Paywall.ARG_FIRST_RUN) ?: false
            var isFinishing by remember { mutableStateOf(false) }

            val onDecision: () -> Unit = {
                if (!firstRun) {
                    navController.popBackStack()
                } else if (!isFinishing) {
                    isFinishing = true
                    // Uses the NavHost-level scope: this screen leaves composition on navigate.
                    // Navigate only after the write so the first run is never lost.
                    scope.launch {
                        runCatching { onboardingPreferences.advanceTo(OnboardingProgress.COMPLETED) }
                        navController.navigate(Routes.Home.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            }

            PaywallRoute(
                onContinueFreeClick = onDecision,
                onPurchased = onDecision
            )
        }

        composable(Routes.CallHistory.route) {
            CallHistoryRoute(
                onBack = { navController.popBackStack() },

            )
        }

        composable(Routes.ContactsConsent.route) {
            ContactsConsentRoute(
                onBack = { navController.popBackStack() },
                onAccepted = {
                    navController.navigate(Routes.ContactsImport.route) {
                        popUpTo(Routes.ContactsConsent.route) { inclusive = true }
                    }
                },
                onOpenPaywall = {
                    navController.navigate(Routes.Paywall.create(firstRun = false))
                },
                onOpenTerms = {
                    navController.navigate(Routes.Terms.route)
                },
                onOpenPrivacyPolicy = {
                    navController.navigate(Routes.PrivacyPolicy.route)
                }
            )
        }

        composable(Routes.ContactsImport.route) {
            ContactsImportRoute(
                onFinished = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.ContactsImport.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Terms.route) {
            LegalScreen(
                title = stringResource(R.string.terms_of_use),
                body = stringResource(R.string.terms_of_use_body),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PrivacyPolicy.route) {
            LegalScreen(
                title = stringResource(R.string.privacy_policy),
                body = stringResource(R.string.privacy_policy_body),
                onBack = { navController.popBackStack() }
            )
        }

    }
}

