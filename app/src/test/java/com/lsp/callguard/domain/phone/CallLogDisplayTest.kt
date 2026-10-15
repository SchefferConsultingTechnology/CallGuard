package com.lsp.callguard.domain.phone

import com.lsp.callguard.domain.model.CallDecisionLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallLogDisplayTest {

    private val normalizer = PhoneNormalizer()
    private val formatter = PhoneNumberDisplayFormatter()

    private fun log(phone: String?, normalized: String?) =
        CallDecisionLog("id", phone, normalized, allowed = false, reason = "BLOCKED_NOT_IN_WHITELIST", createdAt = 0)

    @Test
    fun `canonical number is formatted for the region`() {
        val entry = log("11987654321", "+5511987654321")
        assertEquals("(11) 98765-4321", displayPhoneOf(entry, "BR", normalizer, formatter))
    }

    @Test
    fun `legacy row without canonical number is normalized with the same rule`() {
        val entry = log("11987654321", null)
        assertEquals("+5511987654321", canonicalOf(entry, "BR", normalizer))
        assertEquals("(11) 98765-4321", displayPhoneOf(entry, "BR", normalizer, formatter))
    }

    @Test
    fun `unparseable number is shown as received`() {
        val entry = log("*144", null)
        assertNull(canonicalOf(entry, "BR", normalizer))
        assertEquals("*144", displayPhoneOf(entry, "BR", normalizer, formatter))
    }

    @Test
    fun `hidden number has nothing to show`() {
        assertNull(displayPhoneOf(log(null, null), "BR", normalizer, formatter))
    }
}
