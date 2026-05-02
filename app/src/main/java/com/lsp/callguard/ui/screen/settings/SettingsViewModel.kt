package com.lsp.callguard.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.domain.model.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val appSettings: AppSettings = AppSettings(),
    val isSubscribed: Boolean = false
)

class SettingsViewModel(
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        settingsPreferences.settingsFlow
            .map { settings ->
                SettingsUiState(
                    appSettings = settings,
                    isSubscribed = false
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsUiState()
            )

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