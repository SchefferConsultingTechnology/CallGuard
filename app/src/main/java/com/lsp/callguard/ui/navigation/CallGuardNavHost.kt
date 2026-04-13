package com.lsp.callguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lsp.callguard.ui.screen.home.HomeScreen
import com.lsp.callguard.ui.screen.settings.SettingsScreen
import com.lsp.callguard.ui.screen.whitelist.WhitelistScreen
import com.lsp.callguard.ui.screen.language.LanguageSelectionScreen

@Composable
fun CallGuardNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        //startDestination = Routes.Home.route
        startDestination = "language"
    ) {

        composable("language") {
            LanguageSelectionScreen(
                onConfirm = {
                    // depois a gente navega para Home
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
    }
}

