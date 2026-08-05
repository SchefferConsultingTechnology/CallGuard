package com.lsp.callguard.ui.screen.paywall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.data.billing.BillingRepository
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore

@Composable
fun rememberPaywallViewModel(): PaywallViewModel {
    val context = LocalContext.current.applicationContext

    val factory = remember(context) {
        val licensePreferences = LicensePreferences(context.appPreferencesDataStore)
        val billingRepository = BillingRepository.getInstance(context, licensePreferences)

        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PaywallViewModel(billingRepository) as T
            }
        }
    }

    return viewModel(factory = factory)
}
