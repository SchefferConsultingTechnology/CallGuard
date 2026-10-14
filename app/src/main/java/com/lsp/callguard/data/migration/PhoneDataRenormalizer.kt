package com.lsp.callguard.data.migration

import com.lsp.callguard.data.local.dao.AllowedNumberDao
import com.lsp.callguard.data.local.preferences.PhoneDataPreferences
import com.lsp.callguard.data.repository.DeviceContactCacheRepository
import com.lsp.callguard.domain.model.DeviceContact
import com.lsp.callguard.domain.phone.PhoneNormalizationResult
import com.lsp.callguard.domain.phone.PhoneNormalizer
import com.lsp.callguard.domain.phone.RegionProvider
import kotlin.coroutines.cancellation.CancellationException

/**
 * One-time rewrite of stored numbers whenever the normalization rule changes (FR-007, FR-011,
 * FR-014). Idempotent; the version is only saved after every step succeeded, so an interrupted
 * run is simply retried on the next app start.
 */
class PhoneDataRenormalizer(
    private val allowedNumberDao: AllowedNumberDao,
    private val contactCache: DeviceContactCacheRepository,
    private val preferences: PhoneDataPreferences,
    private val hasContactsConsent: suspend () -> Boolean,
    private val hasContactsPermission: () -> Boolean,
    private val loadDeviceContacts: suspend () -> List<DeviceContact>,
    private val normalizer: PhoneNormalizer,
    private val regionProvider: RegionProvider,
    private val clock: () -> Long = System::currentTimeMillis
) {

    /** Never throws; a failure leaves the version unsaved. */
    suspend fun runIfNeeded(): Result<Unit> {
        return try {
            if (preferences.phoneNormalizationVersion() >= TARGET_VERSION) {
                return Result.success(Unit)
            }

            renormalizeWhitelist()
            refreshContactCache()

            preferences.setPhoneNormalizationVersion(TARGET_VERSION)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun renormalizeWhitelist() {
        val defaultRegion = regionProvider.defaultRegion()
        val entries = allowedNumberDao.getAll()
        val storedPhones = entries.mapTo(mutableSetOf()) { it.phoneE164 }

        for (entry in entries) {
            val normalized = normalizer.normalize(entry.phoneE164, defaultRegion)
                as? PhoneNormalizationResult.Valid
                ?: continue

            if (normalized.phoneE164 == entry.phoneE164) continue

            if (normalized.phoneE164 in storedPhones) {
                // The canonical number is already protected by another entry.
                allowedNumberDao.deleteById(entry.id)
            } else {
                allowedNumberDao.updatePhone(entry.id, normalized.phoneE164, clock())
                storedPhones.add(normalized.phoneE164)
            }

            storedPhones.remove(entry.phoneE164)
        }
    }

    private suspend fun refreshContactCache() {
        if (hasContactsConsent() && hasContactsPermission()) {
            contactCache.replaceAll(loadDeviceContacts())
        } else {
            // Principle II: without consent or permission the cache must not be kept.
            contactCache.clear()
        }
    }

    companion object {
        const val TARGET_VERSION = 1
    }
}
