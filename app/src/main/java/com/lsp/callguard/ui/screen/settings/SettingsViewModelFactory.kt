package com.lsp.callguard.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore

@Composable
fun rememberSettingsViewModel(): SettingsViewModel {
    val context = LocalContext.current.applicationContext

    val factory = remember(context) {
        val preferences = SettingsPreferences(context.appPreferencesDataStore)
        val licensePreferences = LicensePreferences(context.appPreferencesDataStore)


        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(preferences,licensePreferences = licensePreferences) as T
            }
        }
    }

    return viewModel(factory = factory)
}

