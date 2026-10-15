package com.lsp.callguard.domain.model

import com.lsp.callguard.domain.billing.EntitlementStatus

data class LicenseState(
    val tier: Tier = Tier.FREE,
    val status: EntitlementStatus = EntitlementStatus.NONE,
    val hasPendingPurchase: Boolean = false
) {
    val features: PlanFeatures
        get() = PlanFeatures.of(tier)
}
