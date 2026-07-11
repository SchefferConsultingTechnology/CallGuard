package com.lsp.callguard.ui.screen.contacts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import com.lsp.callguard.data.repository.ContactsRepository
import com.lsp.callguard.data.repository.DeviceContactCacheRepository
import com.lsp.callguard.ui.screen.consent.ContactsImportViewModel

@Composable
fun ContactsImportRoute(
    onFinished: () -> Unit
) {
    val context = LocalContext.current.applicationContext

    val viewModel: ContactsImportViewModel = viewModel(
        factory = remember(context) {
            val contactsRepository = ContactsRepository(context)
            val settingsPreferences = SettingsPreferences(context.appPreferencesDataStore)
            val database = CallGuardDatabase.getInstance(context)

            val contactCacheRepository = DeviceContactCacheRepository(
                dao = database.deviceContactDao()
            )

            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ContactsImportViewModel(
                        contactsRepository = contactsRepository,
                        contactCacheRepository = contactCacheRepository,
                        settingsPreferences = settingsPreferences
                    ) as T
                }
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.importContacts(
            onFinished = {
                // não navega automaticamente para o usuário ver o resultado
            }
        )
    }

    ContactsImportScreen(
        uiState = uiState,
        onContinue = onFinished
    )
}
