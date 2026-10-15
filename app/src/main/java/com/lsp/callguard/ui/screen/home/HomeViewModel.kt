package com.lsp.callguard.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.repository.CallDecisionLogRepository
import com.lsp.callguard.data.repository.WhitelistRepository
import com.lsp.callguard.domain.billing.EntitlementStatus
import com.lsp.callguard.domain.model.PlanFeatures
import com.lsp.callguard.domain.model.Tier
import com.lsp.callguard.domain.phone.PhoneNormalizer
import com.lsp.callguard.domain.phone.PhoneNumberDisplayFormatter
import com.lsp.callguard.domain.phone.RegionProvider
import com.lsp.callguard.domain.phone.displayPhoneOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val whitelistCount: Int = 0,
    val tier: Tier = Tier.FREE,
    val status: EntitlementStatus = EntitlementStatus.NONE,
    val hasPendingPurchase: Boolean = false,
    /** Set when the plan went down since the user last saw it; shown once until dismissed. */
    val downgradedFrom: Tier? = null,
    val isProtectionEnabled: Boolean = false,
    /** Up to [RECENT_CALLS] screened calls, newest first. */
    val recentCalls: List<RecentCall> = emptyList()
) {
    val hasPaidPlan: Boolean
        get() = tier != Tier.FREE

    /** Null = unlimited. */
    val whitelistLimit: Int?
        get() = PlanFeatures.of(tier).whitelistLimit

    val progress: Float
        get() = whitelistLimit?.let { whitelistCount / it.toFloat() } ?: 0f
}

/** A row of the Início's recent calls; [displayPhone] is null for hidden numbers. */
data class RecentCall(
    val id: String,
    val displayPhone: String?,
    val allowed: Boolean,
    val createdAt: Long
)

const val RECENT_CALLS = 3

class HomeViewModel(
    repository: WhitelistRepository,
    private val settingsPreferences: SettingsPreferences,
    private val licensePreferences: LicensePreferences,
    callLogRepository: CallDecisionLogRepository,
    private val normalizer: PhoneNormalizer,
    private val formatter: PhoneNumberDisplayFormatter,
    private val regionProvider: RegionProvider
) : ViewModel() {

    private val lastSeenTier = MutableStateFlow<Tier?>(null)
    private val region = MutableStateFlow<String?>(null)

    // Same display rule as the Histórico tab (feature 001), so both show the number the same way.
    private val recentCalls = combine(callLogRepository.observeLatestLogs(), region) { logs, userRegion ->
        logs.take(RECENT_CALLS).map { log ->
            RecentCall(
                id = log.id,
                displayPhone = displayPhoneOf(log, userRegion, normalizer, formatter),
                allowed = log.allowed,
                createdAt = log.createdAt
            )
        }
    }

    init {
        viewModelScope.launch {
            region.value = regionProvider.defaultRegion()
        }

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
            recentCalls,
            lastSeenTier
        ) { whitelistCount, settings, license, recent, seen ->
            HomeUiState(
                whitelistCount = whitelistCount,
                isProtectionEnabled = settings.isProtectionEnabled,
                tier = license.tier,
                status = license.status,
                hasPendingPurchase = license.hasPendingPurchase,
                downgradedFrom = seen?.takeIf { it > license.tier },
                recentCalls = recent
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )
}