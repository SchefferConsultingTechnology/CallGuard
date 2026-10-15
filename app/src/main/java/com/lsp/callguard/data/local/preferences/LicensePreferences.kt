package com.lsp.callguard.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsp.callguard.domain.billing.Entitlement
import com.lsp.callguard.domain.billing.EntitlementStatus
import com.lsp.callguard.domain.model.LicenseState
import com.lsp.callguard.domain.model.Tier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Local cache of the plan resolved from the store (constitution VI: Play is the source of truth).
 * Only overwritten with a successful store answer, so being offline never downgrades a payer.
 */
class LicensePreferences(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val TIER = stringPreferencesKey("tier")
        val PLAN_STATUS = stringPreferencesKey("plan_status")
        val PENDING_PURCHASE = booleanPreferencesKey("pending_purchase")
        val LAST_SEEN_TIER = stringPreferencesKey("last_seen_tier")

        // Before tiers: a single on/off license, only ever set by the debug mock.
        val LEGACY_IS_LICENSED = booleanPreferencesKey("is_licensed")
        val LEGACY_PLAN_NAME = stringPreferencesKey("plan_name")
    }

    val licenseFlow: Flow<LicenseState> =
        dataStore.data.map { preferences ->
            val tier = parseTier(preferences[Keys.TIER])
                ?: if (preferences[Keys.LEGACY_IS_LICENSED] == true) Tier.PLUS else Tier.FREE

            LicenseState(
                tier = tier,
                status = preferences[Keys.PLAN_STATUS]
                    ?.let { stored -> EntitlementStatus.entries.find { it.name == stored } }
                    ?: if (tier == Tier.FREE) EntitlementStatus.NONE else EntitlementStatus.ACTIVE,
                hasPendingPurchase = preferences[Keys.PENDING_PURCHASE] ?: false
            )
        }

    suspend fun save(entitlement: Entitlement) {
        dataStore.edit { preferences ->
            preferences[Keys.TIER] = entitlement.tier.name
            preferences[Keys.PLAN_STATUS] = entitlement.status.name
            preferences[Keys.PENDING_PURCHASE] = entitlement.hasPendingPurchase
            preferences.remove(Keys.LEGACY_IS_LICENSED)
            preferences.remove(Keys.LEGACY_PLAN_NAME)
        }
    }

    /** Last tier the user was shown, to tell them once when they are downgraded. */
    suspend fun lastSeenTier(): Tier? {
        return parseTier(dataStore.data.first()[Keys.LAST_SEEN_TIER])
    }

    suspend fun setLastSeenTier(tier: Tier) {
        dataStore.edit { preferences -> preferences[Keys.LAST_SEEN_TIER] = tier.name }
    }

    /** Debug-only override (constitution VI); callers must guard with BuildConfig.DEBUG. */
    suspend fun setMockTier(tier: Tier) {
        save(
            Entitlement(
                tier = tier,
                status = if (tier == Tier.FREE) EntitlementStatus.NONE else EntitlementStatus.ACTIVE,
                hasPendingPurchase = false
            )
        )
    }

    private fun parseTier(value: String?): Tier? {
        return value?.let { stored -> Tier.entries.find { it.name == stored } }
    }
}
