package com.lsp.callguard.ui.screen.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.SubscriptionPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class ContactsConsentEvent {
    data object RequestContactsPermission : ContactsConsentEvent()
    data object OpenPaywall : ContactsConsentEvent()
}

class ContactsConsentViewModel(
    private val settingsPreferences: SettingsPreferences,
    private val subscriptionPreferences: SubscriptionPreferences
) : ViewModel() {

    fun acceptConsent(onEvent: (ContactsConsentEvent) -> Unit) {
        viewModelScope.launch {
            val subscription = subscriptionPreferences.subscriptionFlow.first()

            if (!subscription.isSubscribed) {
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