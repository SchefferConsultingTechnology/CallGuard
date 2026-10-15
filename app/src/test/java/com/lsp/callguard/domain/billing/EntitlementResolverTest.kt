package com.lsp.callguard.domain.billing

import com.lsp.callguard.domain.model.PlanCatalog
import com.lsp.callguard.domain.model.Tier
import org.junit.Assert.assertEquals
import org.junit.Test

/** Mirrors the resolveEntitlement table in specs/003-premium-plans/contracts/entitlements.md. */
class EntitlementResolverTest {

    private fun purchase(
        product: String,
        state: PurchaseState = PurchaseState.PURCHASED,
        renewing: Boolean = product != PlanCatalog.PLUS_LIFETIME,
        token: String = "token-$product"
    ) = PurchaseSnapshot(listOf(product), state, renewing, token)

    private fun assertResolves(
        purchases: List<PurchaseSnapshot>,
        tier: Tier,
        status: EntitlementStatus,
        pending: Boolean
    ) {
        val entitlement = resolveEntitlement(purchases)
        assertEquals(tier, entitlement.tier)
        assertEquals(status, entitlement.status)
        assertEquals(pending, entitlement.hasPendingPurchase)
    }

    @Test fun `01 nothing is free`() =
        assertResolves(emptyList(), Tier.FREE, EntitlementStatus.NONE, false)

    @Test fun `02 renewing plus`() =
        assertResolves(listOf(purchase(PlanCatalog.PLUS)), Tier.PLUS, EntitlementStatus.ACTIVE, false)

    @Test fun `03 canceled plus stays active until the end`() =
        assertResolves(listOf(purchase(PlanCatalog.PLUS, renewing = false)), Tier.PLUS, EntitlementStatus.CANCELED_ACTIVE, false)

    @Test fun `04 lifetime plus`() =
        assertResolves(listOf(purchase(PlanCatalog.PLUS_LIFETIME)), Tier.PLUS, EntitlementStatus.ACTIVE, false)

    @Test fun `05 renewing pro`() =
        assertResolves(listOf(purchase(PlanCatalog.PRO)), Tier.PRO, EntitlementStatus.ACTIVE, false)

    @Test fun `06 lifetime and pro gives pro`() =
        assertResolves(listOf(purchase(PlanCatalog.PLUS_LIFETIME), purchase(PlanCatalog.PRO)), Tier.PRO, EntitlementStatus.ACTIVE, false)

    @Test fun `07 pending plus unlocks nothing`() =
        assertResolves(listOf(purchase(PlanCatalog.PLUS, PurchaseState.PENDING)), Tier.FREE, EntitlementStatus.NONE, true)

    @Test fun `08 lifetime with pending pro stays plus`() =
        assertResolves(
            listOf(purchase(PlanCatalog.PLUS_LIFETIME), purchase(PlanCatalog.PRO, PurchaseState.PENDING)),
            Tier.PLUS, EntitlementStatus.ACTIVE, true
        )

    @Test fun `09 unknown product is ignored`() =
        assertResolves(listOf(purchase("callguard_premium_monthly")), Tier.FREE, EntitlementStatus.NONE, false)

    @Test fun `10 unspecified state is ignored`() =
        assertResolves(listOf(purchase(PlanCatalog.PLUS, PurchaseState.UNSPECIFIED)), Tier.FREE, EntitlementStatus.NONE, false)

    @Test
    fun `subscription token comes from the highest subscription`() {
        val entitlement = resolveEntitlement(
            listOf(purchase(PlanCatalog.PLUS_LIFETIME), purchase(PlanCatalog.PRO, token = "pro-token"))
        )
        assertEquals("pro-token", entitlement.activeSubscriptionToken)
        assertEquals(PlanCatalog.PRO, entitlement.activeSubscriptionProduct)
        assertEquals(true, entitlement.hasLifetime)
    }

    @Test
    fun `lifetime alone has no subscription token`() {
        val entitlement = resolveEntitlement(listOf(purchase(PlanCatalog.PLUS_LIFETIME)))
        assertEquals(null, entitlement.activeSubscriptionToken)
    }
}
