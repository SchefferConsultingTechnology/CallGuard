package com.lsp.callguard.ui.screen.consent

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.SubscriptionPreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore

@Composable
fun ContactsConsentRoute(
    onBack: () -> Unit,
    onAccepted: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    val context = LocalContext.current.applicationContext

    lateinit var viewModelRef: ContactsConsentViewModel

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModelRef.onContactsPermissionResult(
            granted = granted,
            onFinished = onAccepted
        )
    }

    val viewModel: ContactsConsentViewModel = viewModel(
        factory = remember(context) {
            val settingsPreferences = SettingsPreferences(
                dataStore = context.appPreferencesDataStore
            )

            val subscriptionPreferences = SubscriptionPreferences(
                dataStore = context.appPreferencesDataStore
            )

            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ContactsConsentViewModel(
                        settingsPreferences = settingsPreferences,
                        subscriptionPreferences = subscriptionPreferences
                    ) as T
                }
            }
        }
    )

    viewModelRef = viewModel

    ContactsConsentScreen(
        onBack = onBack,
        onAccept = {
            viewModel.acceptConsent { event ->
                when (event) {
                    ContactsConsentEvent.RequestContactsPermission -> {
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }

                    ContactsConsentEvent.OpenPaywall -> {
                        onOpenPaywall()
                    }
                }
            }
        },
        onOpenTerms = onOpenTerms,
        onOpenPrivacyPolicy = onOpenPrivacyPolicy
    )
}