package com.lsp.callguard.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lsp.callguard.R
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import com.lsp.callguard.data.repository.WhitelistRepository
import com.lsp.callguard.domain.protection.CallScreeningRoleChecker

@Composable
fun HomeRoute(
    onOpenWhitelist: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenProtectionSetup: () -> Unit,
    onOpenCallHistory: () -> Unit,
    onOpenContactsConsent: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val fallbackLabel = stringResource(R.string.contact_no_name)

    val viewModel: HomeViewModel = viewModel(
        factory = remember(context, fallbackLabel) {
            val db = CallGuardDatabase.getInstance(context)
            val repository = WhitelistRepository(db.allowedNumberDao(), fallbackLabel)
            val settingsPreferences = SettingsPreferences(context.appPreferencesDataStore)
            val licensePreferences = LicensePreferences(context.appPreferencesDataStore)
            val deviceContactDao = db.deviceContactDao()


            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(
                        repository = repository,
                        settingsPreferences = settingsPreferences,
                        licensePreferences = licensePreferences,
                        deviceContactDao = deviceContactDao
                    ) as T
                }
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncProtectionState(CallScreeningRoleChecker.isRoleHeld(context))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HomeScreen(
        uiState = uiState,
        onOpenWhitelist = onOpenWhitelist,
        onOpenSettings = onOpenSettings,
        onOpenPaywall = onOpenPaywall,
        onOpenProtectionSetup = onOpenProtectionSetup,
        onOpenCallHistory = onOpenCallHistory,
        onOpenContactsConsent = onOpenContactsConsent
    )
}

