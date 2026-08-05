package com.lsp.callguard.ui.screen.protection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import kotlinx.coroutines.launch

class ProtectionSetupViewModel(
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    fun syncProtectionState(isHeld: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setProtectionEnabled(isHeld)
        }
    }
}
