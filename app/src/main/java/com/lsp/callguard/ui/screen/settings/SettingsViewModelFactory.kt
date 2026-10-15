package com.lsp.callguard.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.core.phone.PhoneServices
import com.lsp.callguard.data.backup.BackupRepository
import com.lsp.callguard.data.billing.BillingRepository
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberSettingsViewModel(): SettingsViewModel {
    val context = LocalContext.current.applicationContext

    val factory = remember(context) {
        val preferences = SettingsPreferences(context.appPreferencesDataStore)
        val licensePreferences = LicensePreferences(context.appPreferencesDataStore)
        val database = CallGuardDatabase.getInstance(context)


        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    settingsPreferences = preferences,
                    licensePreferences = licensePreferences,
                    billingRepository = BillingRepository.getInstance(context, licensePreferences),
                    backupRepository = BackupRepository(
                        allowedNumberDao = database.allowedNumberDao(),
                        callDecisionLogDao = database.callDecisionLogDao(),
                        settingsPreferences = preferences,
                        languagePreferences = LanguagePreferences(context.appPreferencesDataStore),
                        licensePreferences = licensePreferences,
                        normalizer = PhoneServices.normalizer,
                        regionProvider = PhoneServices.regionProvider(context)
                    ),
                    writeFile = { uri, bytes ->
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(bytes) }
                                ?: error("cannot open $uri")
                        }
                    },
                    readFile = { uri ->
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                ?: error("cannot open $uri")
                        }
                    }
                ) as T
            }
        }
    }

    return viewModel(factory = factory)
}

