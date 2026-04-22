package com.lsp.callguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.ui.screen.home.HomeScreen
import com.lsp.callguard.ui.screen.settings.SettingsScreen
import com.lsp.callguard.ui.screen.whitelist.WhitelistScreen
import com.lsp.callguard.ui.screen.language.LanguageSelectionScreen
import com.lsp.callguard.ui.screen.language.rememberLanguageSelectionViewModel
import com.lsp.callguard.ui.screen.onboarding.OnboardingScreen
import com.lsp.callguard.ui.screen.paywall.PaywallScreen
import com.lsp.callguard.core.language.LocaleManagerHelper

@Composable
fun CallGuardNavHost( languagePreferences: LanguagePreferences) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,

        startDestination = Routes.Language.route
    ) {

        composable(Routes.Onboarding.route) {
            OnboardingScreen(
                onContinue = {
                    navController.navigate(Routes.Paywall.route) {
                        popUpTo(Routes.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Language.route) {
            val viewModel = rememberLanguageSelectionViewModel(languagePreferences)
            val uiState by viewModel.uiState.collectAsState()

            LanguageSelectionScreen(
                selectedLanguage = uiState.selectedLanguage,
                onSelectLanguage = { language ->
                    viewModel.onLanguageSelected(language)
                },
                onConfirm = {
                    val selectedLanguage = uiState.selectedLanguage ?: return@LanguageSelectionScreen

                    viewModel.onConfirm {
                        LocaleManagerHelper.applyLanguage(selectedLanguage)

                        navController.navigate(Routes.Onboarding.route) {
                            popUpTo(Routes.Language.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onOpenWhitelist = { navController.navigate(Routes.Whitelist.route) },
                onOpenSettings = { navController.navigate(Routes.Settings.route) }
            )
        }

        composable(Routes.Whitelist.route) {
            WhitelistScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Paywall.route) {
            PaywallScreen(
                onSubscribeClick = {
                    // depois entra a integração com billing
                },
                onContinueFreeClick = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Paywall.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

//@Composable
//fun LanguageSelectionRoute(
//    languagePreferences: LanguagePreferences,
//    onConfirm: () -> Unit
//) {
//    val viewModel = rememberLanguageSelectionViewModel(languagePreferences)
//
//    val uiState by viewModel.uiState.collectAsState()
//
//    LanguageSelectionScreen(
//        selectedLanguage = uiState.selectedLanguage,
//        onSelectLanguage = viewModel::onLanguageSelected,
//        onConfirm = {
//            viewModel.onConfirm {
//                onConfirm()
//            }
//        }
//    )
//}