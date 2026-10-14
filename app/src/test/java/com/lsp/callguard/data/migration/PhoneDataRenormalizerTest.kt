package com.lsp.callguard.data.migration

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lsp.callguard.data.local.entity.AllowedNumberEntity
import com.lsp.callguard.data.local.entity.DeviceContactEntity
import com.lsp.callguard.data.local.preferences.PhoneDataPreferences
import com.lsp.callguard.data.repository.DeviceContactCacheRepository
import com.lsp.callguard.domain.model.DeviceContact
import com.lsp.callguard.domain.phone.PhoneNormalizer
import com.lsp.callguard.fakes.FakeAllowedNumberDao
import com.lsp.callguard.fakes.FakeDeviceContactDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PhoneDataRenormalizerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val legacyMobile = "+551187654321"
    private val canonicalMobile = "+5511987654321"

    private val preferences by lazy {
        PhoneDataPreferences(
            PreferenceDataStoreFactory.create {
                File(tempFolder.root, "phone_data.preferences_pb")
            }
        )
    }

    private val oldContact = DeviceContactEntity(
        id = "old",
        displayName = "Old",
        phoneE164 = "+5511911112222",
        importedAt = 0
    )

    private val deviceContacts = listOf(
        DeviceContact(id = "1", displayName = "Mãe", phoneE164 = canonicalMobile)
    )

    private fun allowed(id: String, phone: String) = AllowedNumberEntity(
        id = id,
        label = id,
        phoneE164 = phone,
        createdAt = 0,
        updatedAt = 0
    )

    private fun renormalizer(
        allowedDao: FakeAllowedNumberDao,
        contactDao: FakeDeviceContactDao = FakeDeviceContactDao(listOf(oldContact)),
        consent: Boolean = true,
        permission: Boolean = true,
        loadContacts: suspend () -> List<DeviceContact> = { deviceContacts }
    ) = PhoneDataRenormalizer(
        allowedNumberDao = allowedDao,
        contactCache = DeviceContactCacheRepository(contactDao),
        preferences = preferences,
        hasContactsConsent = { consent },
        hasContactsPermission = { permission },
        loadDeviceContacts = loadContacts,
        normalizer = PhoneNormalizer(),
        regionProvider = { "BR" },
        clock = { 42L }
    )

    @Test
    fun `a - legacy 8 digit mobile is upgraded`() = runTest {
        val allowedDao = FakeAllowedNumberDao(listOf(allowed("mom", legacyMobile)))

        renormalizer(allowedDao).runIfNeeded()

        assertEquals(listOf(canonicalMobile), allowedDao.current.map { it.phoneE164 })
        assertEquals(42L, allowedDao.current.single().updatedAt)
    }

    @Test
    fun `b - legacy duplicate of a canonical entry is removed`() = runTest {
        val allowedDao = FakeAllowedNumberDao(
            listOf(allowed("legacy", legacyMobile), allowed("canonical", canonicalMobile))
        )

        renormalizer(allowedDao).runIfNeeded()

        assertEquals(listOf("canonical"), allowedDao.current.map { it.id })
    }

    @Test
    fun `c - canonical entries are untouched`() = runTest {
        val entries = listOf(allowed("mom", canonicalMobile), allowed("pt", "+351912345678"))
        val allowedDao = FakeAllowedNumberDao(entries)

        renormalizer(allowedDao).runIfNeeded()

        assertEquals(entries, allowedDao.current)
    }

    @Test
    fun `d - contact cache is replaced when consent and permission are granted`() = runTest {
        val contactDao = FakeDeviceContactDao(listOf(oldContact))

        renormalizer(FakeAllowedNumberDao(), contactDao).runIfNeeded()

        assertEquals(listOf(canonicalMobile), contactDao.current.map { it.phoneE164 })
    }

    @Test
    fun `e - contact cache is cleared without consent or permission`() = runTest {
        for ((index, grants) in listOf(false to true, true to false).withIndex()) {
            val (consent, permission) = grants
            val contactDao = FakeDeviceContactDao(listOf(oldContact))
            val freshPreferences = PhoneDataPreferences(
                PreferenceDataStoreFactory.create { File(tempFolder.root, "case_$index.preferences_pb") }
            )

            PhoneDataRenormalizer(
                allowedNumberDao = FakeAllowedNumberDao(),
                contactCache = DeviceContactCacheRepository(contactDao),
                preferences = freshPreferences,
                hasContactsConsent = { consent },
                hasContactsPermission = { permission },
                loadDeviceContacts = { deviceContacts },
                normalizer = PhoneNormalizer(),
                regionProvider = { "BR" }
            ).runIfNeeded()

            assertTrue(contactDao.current.isEmpty())
            assertEquals(PhoneDataRenormalizer.TARGET_VERSION, freshPreferences.phoneNormalizationVersion())
        }
    }

    @Test
    fun `f - nothing happens when already at target version`() = runTest {
        preferences.setPhoneNormalizationVersion(PhoneDataRenormalizer.TARGET_VERSION)
        val allowedDao = FakeAllowedNumberDao(listOf(allowed("mom", legacyMobile)))
        val contactDao = FakeDeviceContactDao(listOf(oldContact))

        renormalizer(allowedDao, contactDao).runIfNeeded()

        assertEquals(legacyMobile, allowedDao.current.single().phoneE164)
        assertEquals(listOf(oldContact), contactDao.current)
    }

    @Test
    fun `g - failing contact loader keeps previous cache and does not save the version`() = runTest {
        val contactDao = FakeDeviceContactDao(listOf(oldContact))

        val result = renormalizer(
            FakeAllowedNumberDao(),
            contactDao,
            loadContacts = { throw IllegalStateException("provider unavailable") }
        ).runIfNeeded()

        assertTrue(result.isFailure)
        assertEquals(listOf(oldContact), contactDao.current)
        assertEquals(0, preferences.phoneNormalizationVersion())
    }

    @Test
    fun `h - running twice yields the same state`() = runTest {
        val allowedDao = FakeAllowedNumberDao(
            listOf(allowed("legacy", legacyMobile), allowed("canonical", canonicalMobile))
        )
        val contactDao = FakeDeviceContactDao(listOf(oldContact))
        val subject = renormalizer(allowedDao, contactDao)

        // importedAt is the import time, so it is left out of the comparison.
        fun snapshot() = allowedDao.current to contactDao.current.map { it.copy(importedAt = 0) }

        subject.runIfNeeded()
        val afterFirst = snapshot()
        preferences.setPhoneNormalizationVersion(0)
        subject.runIfNeeded()

        assertEquals(afterFirst, snapshot())
    }

    @Test
    fun `i - cache swap never goes through a standalone clear`() = runTest {
        val contactDao = FakeDeviceContactDao(listOf(oldContact))

        renormalizer(FakeAllowedNumberDao(), contactDao).runIfNeeded()

        assertEquals(1, contactDao.replaceAllCalls)
        assertEquals(0, contactDao.clearCalls)
    }
}
