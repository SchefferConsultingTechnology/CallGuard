package com.lsp.callguard.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.lsp.callguard.R
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.OnboardingPreferences
import com.lsp.callguard.domain.model.OnboardingProgress
import com.lsp.callguard.ui.screen.consent.ContactsConsentRoute
import com.lsp.callguard.ui.screen.contacts.ContactsImportRoute
import com.lsp.callguard.ui.screen.history.CallHistoryRoute
import com.lsp.callguard.ui.screen.home.HomeRoute
import com.lsp.callguard.ui.screen.language.LanguageSelectionScreen
import com.lsp.callguard.ui.screen.language.rememberLanguageSelectionViewModel
import com.lsp.callguard.ui.screen.legal.LegalScreen
import com.lsp.callguard.ui.screen.onboarding.OnboardingScreen
import com.lsp.callguard.ui.screen.paywall.PaywallRoute
import com.lsp.callguard.ui.screen.protection.ProtectionSetupRoute
import com.lsp.callguard.ui.screen.settings.SettingsCategory
import com.lsp.callguard.ui.screen.settings.SettingsCategoryRoute
import com.lsp.callguard.ui.screen.settings.SettingsListRoute
import com.lsp.callguard.ui.screen.settings.SettingsViewModel
import com.lsp.callguard.ui.screen.settings.rememberSettingsViewModel
import com.lsp.callguard.ui.screen.whitelist.WhitelistRoute
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun CallGuardNavHost(
    navController: NavHostController,
    languagePreferences: LanguagePreferences,
    onboardingPreferences: OnboardingPreferences,
    startDestination: String,
    tabReselection: SharedFlow<AppTab>,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    fun reselectionOf(tab: AppTab) = tabReselection.filter { it == tab }.map { }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        // Between tabs (and inside Ajustes) nothing animates; task screens use a short fade.
        // Replaces the NavHost default ~700 ms fade, which made tab switches feel slow.
        enterTransition = {
            if (isTabSwitch(initialState, targetState)) EnterTransition.None else fadeIn(tween(TASK_FADE_MS))
        },
        exitTransition = {
            if (isTabSwitch(initialState, targetState)) ExitTransition.None else fadeOut(tween(TASK_FADE_MS))
        },
        popEnterTransition = {
            if (isTabSwitch(initialState, targetState)) EnterTransition.None else fadeIn(tween(TASK_FADE_MS))
        },
        popExitTransition = {
            if (isTabSwitch(initialState, targetState)) ExitTransition.None else fadeOut(tween(TASK_FADE_MS))
        }
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
                    navController.navigateToTab(AppTab.WHITELIST)
                },
                onOpenPaywall = {
                    navController.navigate(Routes.Paywall.create(firstRun = false))
                },
                onOpenProtectionSetup = {
                    navController.navigate(Routes.ProtectionSetup.route)
                },
                onOpenCallHistory = {
                    navController.navigateToTab(AppTab.HISTORY)
                },
                reselection = reselectionOf(AppTab.HOME)
            )
        }

        composable(Routes.Whitelist.route) {
            WhitelistRoute(
                reselection = reselectionOf(AppTab.WHITELIST),
                onOpenPaywall = {
                    navController.navigate(Routes.Paywall.create(firstRun = false))
                }
            )
        }

        navigation(startDestination = Routes.Settings.route, route = Routes.SettingsGraph.route) {
            composable(Routes.Settings.route) { entry ->
                SettingsListRoute(
                    viewModel = settingsViewModelOf(navController, entry),
                    onOpenCategory = { category ->
                        navController.navigate(Routes.SettingsCategoryPage.create(category.key))
                    },
                    reselection = reselectionOf(AppTab.SETTINGS)
                )
            }

            composable(
                route = Routes.SettingsCategoryPage.route,
                arguments = listOf(navArgument(Routes.SettingsCategoryPage.ARG_CATEGORY) { type = NavType.StringType })
            ) { entry ->
                val category = SettingsCategory.fromKey(
                    entry.arguments?.getString(Routes.SettingsCategoryPage.ARG_CATEGORY)
                ) ?: SettingsCategory.PLAN

                SettingsCategoryRoute(
                    category = category,
                    viewModel = settingsViewModelOf(navController, entry),
                    onBack = { navController.popBackStack() },
                    onOpenPaywall = { navController.navigate(Routes.Paywall.create(firstRun = false)) },
                    onOpenLanguage = { navController.navigate(Routes.Language.route) },
                    onOpenTerms = { navController.navigate(Routes.Terms.route) },
                    onOpenPrivacyPolicy = { navController.navigate(Routes.PrivacyPolicy.route) },
                    onOpenContactsConsent = { navController.navigate(Routes.ContactsConsent.route) }
                )
            }
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
                reselection = reselectionOf(AppTab.HISTORY),
                onOpenPaywall = { navController.navigate(Routes.Paywall.create(firstRun = false)) }
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
                // Back to where the consent was opened from (Ajustes > Contatos); the consent
                // already left the stack when the import opened.
                onFinished = { navController.popBackStack() }
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

private const val TASK_FADE_MS = 150

private fun isTabSwitch(from: NavBackStackEntry, to: NavBackStackEntry): Boolean =
    tabFor(from.destination.route) != null && tabFor(to.destination.route) != null

/** The list and every category page share the Ajustes graph's view model (summaries stay in sync). */
@Composable
private fun settingsViewModelOf(navController: NavHostController, entry: NavBackStackEntry): SettingsViewModel {
    val graphEntry = remember(entry) { navController.getBackStackEntry(Routes.SettingsGraph.route) }
    return rememberSettingsViewModel(graphEntry)
}
