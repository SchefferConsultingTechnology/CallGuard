package com.lsp.callguard.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.domain.model.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine

data class SettingsUiState(
    val appSettings: AppSettings = AppSettings(),
    val isLicensed: Boolean = false
)

class SettingsViewModel(
    private val settingsPreferences: SettingsPreferences,
    private val licensePreferences: LicensePreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        combine(
            settingsPreferences.settingsFlow,
            licensePreferences.licenseFlow
        ) { settings, license ->
            SettingsUiState(
                appSettings = settings,
                isLicensed = license.isLicensed
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun setMockLicenseActive(value: Boolean) {
        viewModelScope.launch {
            licensePreferences.setMockLicenseActive(value)
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