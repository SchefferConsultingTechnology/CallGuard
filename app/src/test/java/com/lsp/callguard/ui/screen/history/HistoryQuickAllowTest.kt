package com.lsp.callguard.ui.screen.history

import org.junit.Assert.assertEquals
import org.junit.Test

/** Mirrors the table in specs/004-history-quick-allow/contracts/quick-allow.md. */
class HistoryQuickAllowTest {

    private val phone = "+5511987654321"

    @Test fun `1 blocked and identifiable can be added`() =
        assertEquals(QuickAllowAction.ADD, quickAllowAction(allowed = false, canonical = phone, inWhitelist = emptySet()))

    @Test fun `2 blocked but already whitelisted`() =
        assertEquals(QuickAllowAction.IN_WHITELIST, quickAllowAction(allowed = false, canonical = phone, inWhitelist = setOf(phone)))

    @Test fun `3 allowed and whitelisted`() =
        assertEquals(QuickAllowAction.IN_WHITELIST, quickAllowAction(allowed = true, canonical = phone, inWhitelist = setOf(phone)))

    @Test fun `4 allowed and not whitelisted offers nothing`() =
        assertEquals(QuickAllowAction.NONE, quickAllowAction(allowed = true, canonical = phone, inWhitelist = emptySet()))

    @Test fun `5 hidden or unparseable offers nothing`() =
        assertEquals(QuickAllowAction.NONE, quickAllowAction(allowed = false, canonical = null, inWhitelist = emptySet()))
}
