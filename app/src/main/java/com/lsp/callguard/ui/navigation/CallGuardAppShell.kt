package com.lsp.callguard.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.OnboardingPreferences
import kotlinx.coroutines.flow.MutableSharedFlow

/** Bottom bar around the NavHost (feature 007). The bar shows only on tab destinations. */
@Composable
fun CallGuardAppShell(
    languagePreferences: LanguagePreferences,
    onboardingPreferences: OnboardingPreferences,
    startDestination: String
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTab = tabFor(currentRoute)
    val tabReselection = remember { MutableSharedFlow<AppTab>(extraBufferCapacity = 1) }

    Scaffold(
        // Screens keep handling the status bar themselves; the bar handles the gesture area.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentTab != null) {
                CallGuardNavigationBar(
                    currentTab = currentTab,
                    onSelect = { tab ->
                        when {
                            tab != currentTab -> navController.navigateToTab(tab)
                            // Re-tapping Ajustes on a category page goes back to the list.
                            tab == AppTab.SETTINGS && currentRoute != Routes.Settings.route ->
                                navController.popBackStack(Routes.Settings.route, inclusive = false)
                            else -> tabReselection.tryEmit(tab)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        CallGuardNavHost(
            navController = navController,
            languagePreferences = languagePreferences,
            onboardingPreferences = onboardingPreferences,
            startDestination = startDestination,
            tabReselection = tabReselection,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        )
    }
}

@Composable
private fun CallGuardNavigationBar(
    currentTab: AppTab,
    onSelect: (AppTab) -> Unit
) {
    // With very large fonts only the selected tab keeps its label; all keep it for screen readers.
    val showAllLabels = LocalDensity.current.fontScale <= 1.5f

    NavigationBar {
        AppTab.entries.forEach { tab ->
            val selected = tab == currentTab
            val label = stringResource(tab.label)
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = if (showAllLabels || selected) null else label
                    )
                },
                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                alwaysShowLabel = showAllLabels
            )
        }
    }
}

/**
 * Tab switch: the Início stays at the base so "back" from any tab returns to it, and each tab keeps
 * its own stack and scroll position (saveState/restoreState).
 */
fun NavController.navigateToTab(tab: AppTab) {
    navigate(tab.route) {
        // Not graph.findStartDestination(): on a first run that is the onboarding, which is gone.
        popUpTo(Routes.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
