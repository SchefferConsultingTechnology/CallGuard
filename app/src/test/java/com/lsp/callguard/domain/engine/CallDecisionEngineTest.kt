package com.lsp.callguard.domain.engine

import com.lsp.callguard.data.local.entity.AllowedNumberEntity
import com.lsp.callguard.data.local.entity.DeviceContactEntity
import com.lsp.callguard.domain.model.AppSettings
import com.lsp.callguard.domain.phone.PhoneNormalizer
import com.lsp.callguard.fakes.FakeAllowedNumberDao
import com.lsp.callguard.fakes.FakeDeviceContactDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Mirrors the decision table in specs/001-phone-normalization/contracts/phone-normalizer.md. */
class CallDecisionEngineTest {

    private val brazilianMobile = "+5511987654321"

    private fun allowed(phoneE164: String) = AllowedNumberEntity(
        id = phoneE164,
        label = "Mom",
        phoneE164 = phoneE164,
        createdAt = 0,
        updatedAt = 0
    )

    private fun contact(phoneE164: String) = DeviceContactEntity(
        id = phoneE164,
        displayName = "John",
        phoneE164 = phoneE164,
        importedAt = 0
    )

    private fun engineWith(
        allowedNumbers: List<AllowedNumberEntity> = emptyList(),
        contacts: List<DeviceContactEntity> = emptyList(),
        normalizer: PhoneNormalizer = PhoneNormalizer()
    ) = CallDecisionEngine(
        allowedNumberDao = FakeAllowedNumberDao(allowedNumbers),
        deviceContactDao = FakeDeviceContactDao(contacts),
        normalizer = normalizer
    )

    private suspend fun CallDecisionEngine.decide(
        phone: String?,
        settings: AppSettings = AppSettings(),
        contactsAllowed: Boolean = false,
        defaultRegion: String? = "BR"
    ) = evaluate(
        phone = phone,
        settings = settings,
        contactsAllowed = contactsAllowed,
        defaultRegion = defaultRegion
    )

    private fun assertDecision(expected: CallDecisionReason, allow: Boolean, decision: CallDecision) {
        assertEquals(expected, decision.reason)
        assertEquals(allow, decision.allow)
    }

    // Decision table

    @Test
    fun `private number is blocked when blockPrivateNumbers is enabled`() = runTest {
        val decision = engineWith().decide(null, AppSettings(blockPrivateNumbers = true))
        assertDecision(CallDecisionReason.BLOCKED_PRIVATE_NUMBER, false, decision)
    }

    @Test
    fun `private number is allowed when blockPrivateNumbers is disabled`() = runTest {
        val decision = engineWith().decide("", AppSettings(blockPrivateNumbers = false))
        assertDecision(CallDecisionReason.ALLOWED_PRIVATE_NUMBER_DISABLED, true, decision)
    }

    @Test
    fun `number in whitelist is always allowed`() = runTest {
        val decision = engineWith(allowedNumbers = listOf(allowed(brazilianMobile)))
            .decide(brazilianMobile, AppSettings(blockUnknown = true))
        assertDecision(CallDecisionReason.ALLOWED_BY_WHITELIST, true, decision)
    }

    @Test
    fun `contact match allows call only when the plan allows contacts and the toggle is on`() = runTest {
        val settings = AppSettings(blockUnknown = true, useContactsAutomatically = true)
        val engine = engineWith(contacts = listOf(contact(brazilianMobile)))

        assertDecision(
            CallDecisionReason.ALLOWED_BY_CONTACTS,
            true,
            engine.decide(brazilianMobile, settings, contactsAllowed = true)
        )
        assertDecision(
            CallDecisionReason.BLOCKED_NOT_IN_WHITELIST,
            false,
            engine.decide(brazilianMobile, settings, contactsAllowed = false)
        )
    }

    @Test
    fun `unknown number is blocked when blockUnknown is enabled`() = runTest {
        val decision = engineWith().decide(brazilianMobile, AppSettings(blockUnknown = true))
        assertDecision(CallDecisionReason.BLOCKED_NOT_IN_WHITELIST, false, decision)
    }

    @Test
    fun `unknown number is allowed when blockUnknown is disabled`() = runTest {
        val decision = engineWith().decide(brazilianMobile, AppSettings(blockUnknown = false))
        assertDecision(CallDecisionReason.ALLOWED_UNKNOWN_DISABLED, true, decision)
    }

    @Test
    fun `unparseable number is blocked as unknown when blockUnknown is enabled`() = runTest {
        val decision = engineWith().decide("12345", AppSettings(blockUnknown = true))
        assertDecision(CallDecisionReason.BLOCKED_UNKNOWN_UNPARSEABLE, false, decision)
    }

    @Test
    fun `unparseable number is allowed when blockUnknown is disabled`() = runTest {
        val decision = engineWith().decide("not-a-number", AppSettings(blockUnknown = false))
        assertDecision(CallDecisionReason.ALLOWED_UNPARSEABLE_UNKNOWN_DISABLED, true, decision)
    }

    @Test
    fun `internal normalization failure always allows even with blockUnknown`() = runTest {
        val failingNormalizer = PhoneNormalizer(parse = { _, _ -> throw IllegalStateException("boom") })
        val decision = engineWith(normalizer = failingNormalizer)
            .decide("11987654321", AppSettings(blockUnknown = true))
        assertDecision(CallDecisionReason.ALLOWED_NORMALIZATION_ERROR, true, decision)
    }

    // Regressions: whitelisted family members were blocked when the carrier sent national formats

    @Test
    fun `whitelisted number calling in national formats is allowed`() = runTest {
        val engine = engineWith(allowedNumbers = listOf(allowed(brazilianMobile)))
        val settings = AppSettings(blockUnknown = true)

        for (incoming in listOf("11987654321", "011987654321", "01511987654321", "1187654321")) {
            assertDecision(
                CallDecisionReason.ALLOWED_BY_WHITELIST,
                true,
                engine.decide(incoming, settings)
            )
        }
    }

    @Test
    fun `contact calling with trunk prefix is allowed`() = runTest {
        val decision = engineWith(contacts = listOf(contact(brazilianMobile))).decide(
            "011987654321",
            AppSettings(blockUnknown = true, useContactsAutomatically = true),
            contactsAllowed = true
        )
        assertDecision(CallDecisionReason.ALLOWED_BY_CONTACTS, true, decision)
    }

    @Test
    fun `national number without any region is allowed when blockUnknown is disabled`() = runTest {
        val decision = engineWith().decide(
            "11987654321",
            AppSettings(blockUnknown = false),
            defaultRegion = null
        )
        assertEquals(true, decision.allow)
    }
}
