package com.lsp.callguard.domain.billing

import com.lsp.callguard.domain.model.PlanCatalog
import com.lsp.callguard.domain.model.Tier

enum class EntitlementStatus {
    ACTIVE,
    /** Still valid but will not renew (canceled in the store). */
    CANCELED_ACTIVE,
    NONE
}

data class Entitlement(
    val tier: Tier,
    val status: EntitlementStatus,
    val hasPendingPurchase: Boolean,
    val activeSubscriptionToken: String? = null,
    val activeSubscriptionProduct: String? = null,
    val hasLifetime: Boolean = false
) {
    companion object {
        val FREE = Entitlement(Tier.FREE, EntitlementStatus.NONE, hasPendingPurchase = false)
    }
}

/**
 * Plan granted by what the store returned. Only PURCHASED items count; the highest tier wins, so a
 * lifetime Plus keeps the user on Plus when a Pro subscription ends.
 */
fun resolveEntitlement(purchases: List<PurchaseSnapshot>): Entitlement {
    val hasPending = purchases.any { it.state == PurchaseState.PENDING }
    val owned = purchases.filter { it.state == PurchaseState.PURCHASED }

    val grants = owned.flatMap { purchase ->
        purchase.productIds.mapNotNull { productId ->
            PlanCatalog.tierOf(productId)?.let { tier -> Grant(tier, productId, purchase) }
        }
    }

    val best = grants.maxByOrNull { it.tier.ordinal }
        ?: return Entitlement.FREE.copy(hasPendingPurchase = hasPending)

    val subscription = grants
        .filter { it.productId != PlanCatalog.PLUS_LIFETIME }
        .maxByOrNull { it.tier.ordinal }

    val status = if (best.productId != PlanCatalog.PLUS_LIFETIME && !best.purchase.isAutoRenewing) {
        EntitlementStatus.CANCELED_ACTIVE
    } else {
        EntitlementStatus.ACTIVE
    }

    return Entitlement(
        tier = best.tier,
        status = status,
        hasPendingPurchase = hasPending,
        activeSubscriptionToken = subscription?.purchase?.purchaseToken,
        activeSubscriptionProduct = subscription?.productId,
        hasLifetime = grants.any { it.productId == PlanCatalog.PLUS_LIFETIME }
    )
}

private data class Grant(val tier: Tier, val productId: String, val purchase: PurchaseSnapshot)
