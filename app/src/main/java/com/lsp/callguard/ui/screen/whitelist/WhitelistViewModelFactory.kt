package com.lsp.callguard.ui.screen.whitelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.R
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import com.lsp.callguard.data.repository.WhitelistRepository

@Composable
fun rememberWhitelistViewModel(): WhitelistViewModel {
    val context = LocalContext.current.applicationContext
    val fallbackLabel = stringResource(R.string.contact_no_name)

    val factory = remember(context, fallbackLabel) {
        val database = CallGuardDatabase.getInstance(context)
        val repository = WhitelistRepository(database.allowedNumberDao(), fallbackLabel)
        val licensePreferences = LicensePreferences(
            dataStore = context.appPreferencesDataStore
        )

        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WhitelistViewModel(repository,
                    licensePreferences = licensePreferences
                    ) as T
            }
        }
    }

    return viewModel(factory = factory)
}

