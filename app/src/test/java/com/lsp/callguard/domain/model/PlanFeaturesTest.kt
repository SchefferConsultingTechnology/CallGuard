package com.lsp.callguard.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** Mirrors the PlanFeatures table in specs/003-premium-plans/data-model.md. */
class PlanFeaturesTest {

    @Test
    fun `free tier`() {
        assertEquals(PlanFeatures(5, contactsAllowed = false, historyRetentionDays = 7, driveBackup = false), PlanFeatures.of(Tier.FREE))
    }

    @Test
    fun `plus tier`() {
        assertEquals(PlanFeatures(null, contactsAllowed = true, historyRetentionDays = 90, driveBackup = false), PlanFeatures.of(Tier.PLUS))
    }

    @Test
    fun `pro tier`() {
        assertEquals(PlanFeatures(null, contactsAllowed = true, historyRetentionDays = 365, driveBackup = true), PlanFeatures.of(Tier.PRO))
    }

    @Test
    fun `product ids map to tiers`() {
        assertEquals(Tier.PLUS, PlanCatalog.tierOf(PlanCatalog.PLUS))
        assertEquals(Tier.PLUS, PlanCatalog.tierOf(PlanCatalog.PLUS_LIFETIME))
        assertEquals(Tier.PRO, PlanCatalog.tierOf(PlanCatalog.PRO))
        assertEquals(null, PlanCatalog.tierOf("callguard_premium_monthly"))
    }
}
