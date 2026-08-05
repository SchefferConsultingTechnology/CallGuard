package com.lsp.callguard.ui.screen.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.lsp.callguard.data.billing.BillingRepository
import kotlinx.coroutines.flow.StateFlow

class PaywallViewModel(
    private val billingRepository: BillingRepository
) : ViewModel() {

    val isPremiumActive: StateFlow<Boolean> = billingRepository.isPremiumActive

    fun launchPurchase(activity: Activity) {
        billingRepository.launchPurchaseFlow(activity)
    }
}
