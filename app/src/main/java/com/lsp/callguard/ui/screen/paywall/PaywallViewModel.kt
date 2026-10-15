package com.lsp.callguard.ui.screen.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.billing.BillingRepository
import com.lsp.callguard.data.billing.CatalogState
import com.lsp.callguard.data.billing.PurchaseOption
import com.lsp.callguard.data.billing.RestoreResult
import com.lsp.callguard.data.billing.StoreOffer
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.domain.billing.EntitlementStatus
import com.lsp.callguard.domain.model.PlanCatalog
import com.lsp.callguard.domain.model.Tier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BillingPeriod { MONTHLY, ANNUAL }

data class PaywallUiState(
    val currentTier: Tier = Tier.FREE,
    val status: EntitlementStatus = EntitlementStatus.NONE,
    val pendingPurchase: Boolean = false,
    val period: BillingPeriod = BillingPeriod.ANNUAL,
    val plusMonthly: StoreOffer? = null,
    val plusAnnual: StoreOffer? = null,
    val plusLifetime: StoreOffer? = null,
    val proMonthly: StoreOffer? = null,
    val proAnnual: StoreOffer? = null,
    val proOnSale: Boolean = PlanCatalog.PRO_ON_SALE,
    val storeLoading: Boolean = true,
    val storeUnavailable: Boolean = false
) {
    val plusOffer: StoreOffer?
        get() = if (period == BillingPeriod.MONTHLY) plusMonthly else plusAnnual

    val proOffer: StoreOffer?
        get() = if (period == BillingPeriod.MONTHLY) proMonthly else proAnnual
}

sealed interface PaywallEvent {
    data class Restore(val result: RestoreResult) : PaywallEvent
    data object PurchaseUnavailable : PaywallEvent
}

class PaywallViewModel(
    private val billingRepository: BillingRepository,
    licensePreferences: LicensePreferences
) : ViewModel() {

    private val period = MutableStateFlow(BillingPeriod.ANNUAL)

    private val _events = MutableSharedFlow<PaywallEvent>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<PaywallUiState> =
        combine(
            billingRepository.catalog,
            licensePreferences.licenseFlow,
            period
        ) { catalog, license, selectedPeriod ->
            val offers = (catalog as? CatalogState.Ready)?.offers.orEmpty()
            fun find(product: String, basePlan: String?) =
                offers.find { it.option == PurchaseOption(product, basePlan) }

            PaywallUiState(
                currentTier = license.tier,
                status = license.status,
                pendingPurchase = license.hasPendingPurchase,
                period = selectedPeriod,
                plusMonthly = find(PlanCatalog.PLUS, PlanCatalog.BASE_MONTHLY),
                plusAnnual = find(PlanCatalog.PLUS, PlanCatalog.BASE_ANNUAL),
                plusLifetime = find(PlanCatalog.PLUS_LIFETIME, null),
                proMonthly = find(PlanCatalog.PRO, PlanCatalog.BASE_MONTHLY),
                proAnnual = find(PlanCatalog.PRO, PlanCatalog.BASE_ANNUAL),
                storeLoading = catalog == CatalogState.Loading,
                storeUnavailable = catalog == CatalogState.Unavailable
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PaywallUiState()
        )

    fun selectPeriod(value: BillingPeriod) {
        period.value = value
    }

    fun buy(activity: Activity, offer: StoreOffer) {
        if (!billingRepository.purchase(activity, offer.option)) {
            viewModelScope.launch { _events.emit(PaywallEvent.PurchaseUnavailable) }
        }
    }

    fun retry() {
        billingRepository.retryCatalog()
    }

    fun restore() {
        viewModelScope.launch {
            _events.emit(PaywallEvent.Restore(billingRepository.restorePurchases()))
        }
    }
}
