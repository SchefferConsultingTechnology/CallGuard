package com.lsp.callguard.data.local.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.lsp.callguard.domain.billing.Entitlement
import com.lsp.callguard.domain.billing.EntitlementStatus
import com.lsp.callguard.domain.model.LicenseState
import com.lsp.callguard.domain.model.Tier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LicensePreferencesTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dataStore by lazy {
        PreferenceDataStoreFactory.create { File(tempFolder.root, "license.preferences_pb") }
    }

    private val preferences by lazy { LicensePreferences(dataStore) }

    @Test
    fun `empty store is free`() = runTest {
        assertEquals(LicenseState(Tier.FREE, EntitlementStatus.NONE, false), preferences.licenseFlow.first())
    }

    @Test
    fun `legacy licensed flag migrates to plus and is removed on save`() = runTest {
        dataStore.edit { it[booleanPreferencesKey("is_licensed")] = true }

        assertEquals(Tier.PLUS, preferences.licenseFlow.first().tier)

        preferences.save(Entitlement.FREE)
        assertNull(dataStore.data.first()[booleanPreferencesKey("is_licensed")])
        assertEquals(Tier.FREE, preferences.licenseFlow.first().tier)
    }

    @Test
    fun `saved entitlement is read back`() = runTest {
        preferences.save(Entitlement(Tier.PRO, EntitlementStatus.CANCELED_ACTIVE, hasPendingPurchase = true))

        assertEquals(
            LicenseState(Tier.PRO, EntitlementStatus.CANCELED_ACTIVE, hasPendingPurchase = true),
            preferences.licenseFlow.first()
        )
    }

    @Test
    fun `last seen tier round trip`() = runTest {
        assertNull(preferences.lastSeenTier())
        preferences.setLastSeenTier(Tier.PLUS)
        assertEquals(Tier.PLUS, preferences.lastSeenTier())
    }

    @Test
    fun `mock tier is stored as active`() = runTest {
        preferences.setMockTier(Tier.PLUS)
        assertEquals(LicenseState(Tier.PLUS, EntitlementStatus.ACTIVE, false), preferences.licenseFlow.first())
    }
}
