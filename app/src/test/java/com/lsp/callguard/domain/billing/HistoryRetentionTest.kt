package com.lsp.callguard.domain.billing

import com.lsp.callguard.domain.model.Tier
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryRetentionTest {

    private val now = 1_800_000_000_000L
    private val day = 86_400_000L

    @Test
    fun `cutoff follows the tier retention`() {
        assertEquals(now - 7 * day, historyCutoff(now, Tier.FREE))
        assertEquals(now - 90 * day, historyCutoff(now, Tier.PLUS))
        assertEquals(now - 365 * day, historyCutoff(now, Tier.PRO))
    }
}
