package com.lsp.callguard.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.ui.graphics.vector.ImageVector
import com.lsp.callguard.R

/** The four bottom-bar destinations (feature 007). [route] is what the bar navigates to. */
enum class AppTab(val route: String, @param:StringRes val label: Int) {
    HOME(Routes.Home.route, R.string.tab_home),
    WHITELIST(Routes.Whitelist.route, R.string.tab_whitelist),
    HISTORY(Routes.CallHistory.route, R.string.tab_history),
    SETTINGS(Routes.SettingsGraph.route, R.string.tab_settings);

    val icon: ImageVector
        get() = when (this) {
            HOME -> Icons.Outlined.Home
            WHITELIST -> Icons.Outlined.VerifiedUser
            HISTORY -> Icons.Outlined.History
            SETTINGS -> Icons.Outlined.Settings
        }

    val selectedIcon: ImageVector
        get() = when (this) {
            HOME -> Icons.Filled.Home
            WHITELIST -> Icons.Filled.VerifiedUser
            HISTORY -> Icons.Filled.History
            SETTINGS -> Icons.Filled.Settings
        }
}

/** Tab a destination belongs to, or null for task screens, which hide the bar. */
fun tabFor(route: String?): AppTab? = when {
    route == null -> null
    route == Routes.Home.route -> AppTab.HOME
    route == Routes.Whitelist.route -> AppTab.WHITELIST
    route == Routes.CallHistory.route -> AppTab.HISTORY
    route == Routes.Settings.route || route.startsWith("${Routes.Settings.route}/") -> AppTab.SETTINGS
    else -> null
}
