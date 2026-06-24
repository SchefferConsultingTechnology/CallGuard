package com.lsp.callguard.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.SubscriptionPreferences
import com.lsp.callguard.domain.model.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine

data class SettingsUiState(
    val appSettings: AppSettings = AppSettings(),
    val isSubscribed: Boolean = false
)

class SettingsViewModel(
    private val settingsPreferences: SettingsPreferences,
    private val subscriptionPreferences: SubscriptionPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        combine(
            settingsPreferences.settingsFlow,
            subscriptionPreferences.subscriptionFlow
        ) { settings, subscription ->
            SettingsUiState(
                appSettings = settings,
                isSubscribed = subscription.isSubscribed
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun setMockSubscriptionActive(value: Boolean) {
        viewModelScope.launch {
            subscriptionPreferences.setMockSubscriptionActive(value)
        }
    }
    fun setBlockUnknown(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setBlockUnknown(value)
        }
    }

    fun setBlockPrivateNumbers(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setBlockPrivateNumbers(value)
        }
    }

    fun setUseContactsAutomatically(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setUseContactsAutomatically(value)
        }
    }
}