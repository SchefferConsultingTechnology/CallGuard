package com.lsp.callguard.domain.engine

import com.lsp.callguard.data.local.entity.AllowedNumberEntity
import com.lsp.callguard.data.local.entity.DeviceContactEntity
import com.lsp.callguard.domain.model.AppSettings
import com.lsp.callguard.fakes.FakeAllowedNumberDao
import com.lsp.callguard.fakes.FakeDeviceContactDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CallDecisionEngineTest {

    private fun engineWith(
        allowedNumbers: List<AllowedNumberEntity> = emptyList(),
        contacts: List<DeviceContactEntity> = emptyList()
    ) = CallDecisionEngine(
        allowedNumberDao = FakeAllowedNumberDao(allowedNumbers),
        deviceContactDao = FakeDeviceContactDao(contacts)
    )

    @Test
    fun `number in whitelist is always allowed`() = runTest {
        val engine = engineWith(
            allowedNumbers = listOf(
                AllowedNumberEntity(
                    id = "1",
                    label = "Mom",
                    phoneE164 = "+15551234567",
                    createdAt = 0,
                    updatedAt = 0
                )
            )
        )

        val decision = engine.evaluate(
            phone = "+15551234567",
            settings = AppSettings(blockUnknown = true),
            isLicensed = false
        )

        assertEquals(true, decision.allow)
        assertEquals(CallDecisionReason.ALLOWED_BY_WHITELIST, decision.reason)
    }

    @Test
    fun `unknown number is blocked when blockUnknown is enabled`() = runTest {
        val engine = engineWith()

        val decision = engine.evaluate(
            phone = "+15551234567",
            settings = AppSettings(blockUnknown = true),
            isLicensed = false
        )

        assertEquals(false, decision.allow)
        assertEquals(CallDecisionReason.BLOCKED_NOT_IN_WHITELIST, decision.reason)
    }

    @Test
    fun `unknown number is allowed when blockUnknown is disabled`() = runTest {
        val engine = engineWith()

        val decision = engine.evaluate(
            phone = "+15551234567",
            settings = AppSettings(blockUnknown = false),
            isLicensed = false
        )

        assertEquals(true, decision.allow)
        assertEquals(CallDecisionReason.ALLOWED_UNKNOWN_DISABLED, decision.reason)
    }

    @Test
    fun `private number is blocked when blockPrivateNumbers is enabled`() = runTest {
        val engine = engineWith()

        val decision = engine.evaluate(
            phone = null,
            settings = AppSettings(blockPrivateNumbers = true),
            isLicensed = false
        )

        assertEquals(false, decision.allow)
        assertEquals(CallDecisionReason.BLOCKED_PRIVATE_NUMBER, decision.reason)
    }

    @Test
    fun `private number is allowed when blockPrivateNumbers is disabled`() = runTest {
        val engine = engineWith()

        val decision = engine.evaluate(
            phone = "",
            settings = AppSettings(blockPrivateNumbers = false),
            isLicensed = false
        )

        assertEquals(true, decision.allow)
        assertEquals(CallDecisionReason.ALLOWED_PRIVATE_NUMBER_DISABLED, decision.reason)
    }

    @Test
    fun `invalid number is blocked`() = runTest {
        val engine = engineWith()

        val decision = engine.evaluate(
            phone = "not-a-number",
            settings = AppSettings(),
            isLicensed = false
        )

        assertEquals(false, decision.allow)
        assertEquals(CallDecisionReason.INVALID_NUMBER, decision.reason)
    }

    @Test
    fun `contact match allows call only when licensed and useContactsAutomatically is on`() = runTest {
        val contacts = listOf(
            DeviceContactEntity(
                id = "1",
                displayName = "John",
                phoneE164 = "+15551234567",
                importedAt = 0
            )
        )

        val licensedEngine = engineWith(contacts = contacts)
        val allowedByContacts = licensedEngine.evaluate(
            phone = "+15551234567",
            settings = AppSettings(blockUnknown = true, useContactsAutomatically = true),
            isLicensed = true
        )
        assertEquals(true, allowedByContacts.allow)
        assertEquals(CallDecisionReason.ALLOWED_BY_CONTACTS, allowedByContacts.reason)

        val unlicensedEngine = engineWith(contacts = contacts)
        val blockedWithoutLicense = unlicensedEngine.evaluate(
            phone = "+15551234567",
            settings = AppSettings(blockUnknown = true, useContactsAutomatically = true),
            isLicensed = false
        )
        assertEquals(false, blockedWithoutLicense.allow)
        assertEquals(CallDecisionReason.BLOCKED_NOT_IN_WHITELIST, blockedWithoutLicense.reason)
    }
}
