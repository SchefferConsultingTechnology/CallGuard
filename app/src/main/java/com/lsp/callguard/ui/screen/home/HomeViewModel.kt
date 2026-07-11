package com.lsp.callguard.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.dao.DeviceContactDao
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.repository.WhitelistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val FREE_WHITELIST_LIMIT = 5

data class HomeUiState(
    val whitelistCount: Int = 0,
    val whitelistLimit: Int = FREE_WHITELIST_LIMIT,
    val isLicensed: Boolean = false,
    val isProtectionEnabled: Boolean = false,
    val useContactsAutomatically: Boolean = false,
    val importedContactsCount: Int = 0
) {
    val progress: Float
        get() = whitelistCount / whitelistLimit.toFloat()
}

class HomeViewModel(
    repository: WhitelistRepository,
    settingsPreferences: SettingsPreferences,
    licensePreferences: LicensePreferences,
    deviceContactDao: DeviceContactDao

) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(
            repository.observeCount(),
            settingsPreferences.settingsFlow,
            licensePreferences.licenseFlow,
            deviceContactDao.observeCount()
        ) { whitelistCount, settings,license, contactsCount ->
            HomeUiState(
                whitelistCount = whitelistCount,
                isProtectionEnabled = settings.isProtectionEnabled,
                isLicensed = license.isLicensed,
                useContactsAutomatically = settings.useContactsAutomatically,
                importedContactsCount = contactsCount
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )
}