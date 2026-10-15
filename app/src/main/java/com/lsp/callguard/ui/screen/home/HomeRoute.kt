package com.lsp.callguard.ui.screen.home

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.R
import com.lsp.callguard.core.phone.PhoneServices
import com.lsp.callguard.data.billing.BillingRepository
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import com.lsp.callguard.data.repository.CallDecisionLogRepository
import com.lsp.callguard.data.repository.WhitelistRepository
import com.lsp.callguard.domain.protection.CallScreeningRoleChecker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun HomeRoute(
    onOpenWhitelist: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenProtectionSetup: () -> Unit,
    onOpenCallHistory: () -> Unit,
    reselection: Flow<Unit> = emptyFlow()
) {
    val context = LocalContext.current.applicationContext
    val fallbackLabel = stringResource(R.string.contact_no_name)

    val viewModel: HomeViewModel = viewModel(
        factory = remember(context, fallbackLabel) {
            val db = CallGuardDatabase.getInstance(context)
            val repository = WhitelistRepository(db.allowedNumberDao(), fallbackLabel)
            val settingsPreferences = SettingsPreferences(context.appPreferencesDataStore)
            val licensePreferences = LicensePreferences(context.appPreferencesDataStore)
            val callLogRepository = CallDecisionLogRepository(db.callDecisionLogDao(), PhoneServices.normalizer)
            val regionProvider = PhoneServices.regionProvider(context)


            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(
                        repository = repository,
                        settingsPreferences = settingsPreferences,
                        licensePreferences = licensePreferences,
                        callLogRepository = callLogRepository,
                        normalizer = PhoneServices.normalizer,
                        formatter = PhoneServices.formatter,
                        regionProvider = regionProvider
                    ) as T
                }
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    // Play's own prompts for declined payments / grace period (no server; research R3).
    val activity = LocalActivity.current
    LaunchedEffect(activity) {
        activity?.let {
            BillingRepository.getInstance(context, LicensePreferences(context.appPreferencesDataStore))
                .showPaymentMessages(it)
        }
    }

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
        onOpenPaywall = onOpenPaywall,
        onOpenProtectionSetup = onOpenProtectionSetup,
        onOpenCallHistory = onOpenCallHistory,
        onDismissDowngradeNotice = viewModel::dismissDowngradeNotice,
        reselection = reselection
    )
}

