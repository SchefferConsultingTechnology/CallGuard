package com.lsp.callguard.ui.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Whitelist : Routes("whitelist")
    data object Settings : Routes("settings")
}

