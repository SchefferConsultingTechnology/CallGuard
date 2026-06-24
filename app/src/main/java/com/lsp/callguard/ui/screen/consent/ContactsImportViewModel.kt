package com.lsp.callguard.ui.screen.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.repository.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContactsImportUiState(
    val isLoading: Boolean = false,
    val importedCount: Int = 0,
    val errorMessage: String? = null
)

class ContactsImportViewModel(
    private val contactsRepository: ContactsRepository,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsImportUiState())
    val uiState: StateFlow<ContactsImportUiState> = _uiState.asStateFlow()

    fun importContacts(onFinished: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value = ContactsImportUiState(isLoading = true)

                val contacts = contactsRepository.getDeviceContacts()

                settingsPreferences.setUseContactsAutomatically(true)

                _uiState.value = ContactsImportUiState(
                    isLoading = false,
                    importedCount = contacts.size
                )

                onFinished()
            } catch (error: Exception) {
                _uiState.value = ContactsImportUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "Não foi possível ler os contatos."
                )
            }
        }
    }
}

