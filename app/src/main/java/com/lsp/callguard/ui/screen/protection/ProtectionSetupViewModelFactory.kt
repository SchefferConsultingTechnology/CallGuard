package com.lsp.callguard.ui.screen.protection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore

@Composable
fun rememberProtectionSetupViewModel(): ProtectionSetupViewModel {
    val context = LocalContext.current.applicationContext

    val factory = remember(context) {
        val settingsPreferences = SettingsPreferences(context.appPreferencesDataStore)

        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProtectionSetupViewModel(settingsPreferences) as T
            }
        }
    }

    return viewModel(factory = factory)
}
