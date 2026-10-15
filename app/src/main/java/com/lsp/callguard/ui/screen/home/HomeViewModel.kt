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
import kotlinx.coroutines.launch
import com.lsp.callguard.domain.model.PlanFeatures
import com.lsp.callguard.domain.model.Tier
import com.lsp.callguard.domain.billing.EntitlementStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class HomeUiState(
    val whitelistCount: Int = 0,
    val tier: Tier = Tier.FREE,
    val status: EntitlementStatus = EntitlementStatus.NONE,
    val hasPendingPurchase: Boolean = false,
    /** Set when the plan went down since the user last saw it; shown once until dismissed. */
    val downgradedFrom: Tier? = null,
    val isProtectionEnabled: Boolean = false,
    val useContactsAutomatically: Boolean = false,
    val importedContactsCount: Int = 0
) {
    val hasPaidPlan: Boolean
        get() = tier != Tier.FREE

    /** Null = unlimited. */
    val whitelistLimit: Int?
        get() = PlanFeatures.of(tier).whitelistLimit

    val progress: Float
        get() = whitelistLimit?.let { whitelistCount / it.toFloat() } ?: 0f
}

class HomeViewModel(
    repository: WhitelistRepository,
    private val settingsPreferences: SettingsPreferences,
    private val licensePreferences: LicensePreferences,
    deviceContactDao: DeviceContactDao

) : ViewModel() {

    private val lastSeenTier = MutableStateFlow<Tier?>(null)

    init {
        // An upgrade (or the first launch) is recorded silently; a downgrade waits for dismissal.
        viewModelScope.launch {
            licensePreferences.licenseFlow.map { it.tier }.distinctUntilChanged().collect { tier ->
                val seen = licensePreferences.lastSeenTier()
                if (seen == null || tier > seen) {
                    licensePreferences.setLastSeenTier(tier)
                    lastSeenTier.value = tier
                } else {
                    lastSeenTier.value = seen
                }
            }
        }
    }

    fun dismissDowngradeNotice() {
        viewModelScope.launch {
            val tier = licensePreferences.licenseFlow.first().tier
            licensePreferences.setLastSeenTier(tier)
            lastSeenTier.value = tier
        }
    }

    fun syncProtectionState(isHeld: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setProtectionEnabled(isHeld)
        }
    }

    val uiState: StateFlow<HomeUiState> =
        combine(
            repository.observeCount(),
            settingsPreferences.settingsFlow,
            licensePreferences.licenseFlow,
            deviceContactDao.observeCount(),
            lastSeenTier
        ) { whitelistCount, settings, license, contactsCount, seen ->
            HomeUiState(
                whitelistCount = whitelistCount,
                isProtectionEnabled = settings.isProtectionEnabled,
                tier = license.tier,
                status = license.status,
                hasPendingPurchase = license.hasPendingPurchase,
                downgradedFrom = seen?.takeIf { it > license.tier },
                useContactsAutomatically = settings.useContactsAutomatically,
                importedContactsCount = contactsCount
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )
}