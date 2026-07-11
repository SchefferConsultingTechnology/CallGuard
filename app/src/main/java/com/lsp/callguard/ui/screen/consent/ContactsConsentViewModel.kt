package com.lsp.callguard.ui.screen.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.LicensePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class ContactsConsentEvent {
    data object RequestContactsPermission : ContactsConsentEvent()
    data object OpenPaywall : ContactsConsentEvent()
}

class ContactsConsentViewModel(
    private val settingsPreferences: SettingsPreferences,
    private val licensePreferences: LicensePreferences
) : ViewModel() {

    fun acceptConsent(onEvent: (ContactsConsentEvent) -> Unit) {
        viewModelScope.launch {
            val license = licensePreferences.licenseFlow.first()

            if (!license.isLicensed) {
                onEvent(ContactsConsentEvent.OpenPaywall)
                return@launch
            }

            settingsPreferences.setHasAcceptedContactsConsent(true)
            onEvent(ContactsConsentEvent.RequestContactsPermission)
        }
    }

    fun onContactsPermissionResult(granted: Boolean, onFinished: () -> Unit) {
        viewModelScope.launch {
            if (granted) {
                settingsPreferences.setUseContactsAutomatically(true)
            }

            onFinished()
        }
    }
}