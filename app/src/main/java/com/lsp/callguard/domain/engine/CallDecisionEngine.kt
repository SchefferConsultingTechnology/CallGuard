package com.lsp.callguard.domain.engine

import com.lsp.callguard.data.local.dao.AllowedNumberDao
import com.lsp.callguard.data.local.dao.DeviceContactDao
import com.lsp.callguard.domain.model.AppSettings
import com.lsp.callguard.domain.phone.PhoneNormalizationError
import com.lsp.callguard.domain.phone.PhoneNormalizationResult
import com.lsp.callguard.domain.phone.PhoneNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallDecisionEngine(
    private val allowedNumberDao: AllowedNumberDao,
    private val deviceContactDao: DeviceContactDao,
    private val normalizer: PhoneNormalizer
) {

    suspend fun evaluate(
        phone: String?,
        settings: AppSettings,
        isLicensed: Boolean,
        defaultRegion: String?
    ): CallDecision {
        return withContext(Dispatchers.IO) {
            if (phone.isNullOrBlank()) {
                return@withContext if (settings.blockPrivateNumbers) {
                    CallDecision(false, CallDecisionReason.BLOCKED_PRIVATE_NUMBER)
                } else {
                    CallDecision(true, CallDecisionReason.ALLOWED_PRIVATE_NUMBER_DISABLED)
                }
            }

            val normalized = normalizer.normalize(phone, defaultRegion)

            when (normalized) {
                is PhoneNormalizationResult.Invalid -> when {
                    // Fail-open: a bug while reading the number must never block a call.
                    normalized.reason == PhoneNormalizationError.INTERNAL_ERROR -> {
                        CallDecision(true, CallDecisionReason.ALLOWED_NORMALIZATION_ERROR)
                    }

                    // An unreadable number is just an unknown caller, never blocked for its format.
                    settings.blockUnknown -> {
                        CallDecision(false, CallDecisionReason.BLOCKED_UNKNOWN_UNPARSEABLE)
                    }

                    else -> {
                        CallDecision(true, CallDecisionReason.ALLOWED_UNPARSEABLE_UNKNOWN_DISABLED)
                    }
                }

                is PhoneNormalizationResult.Valid -> {
                    val phoneE164 = normalized.phoneE164

                    val existsInWhitelist =
                        allowedNumberDao.countByPhone(phoneE164) > 0

                    if (existsInWhitelist) {
                        return@withContext CallDecision(
                            allow = true,
                            reason = CallDecisionReason.ALLOWED_BY_WHITELIST
                        )
                    }

                    val canUseContacts =
                        isLicensed && settings.useContactsAutomatically

                    if (canUseContacts) {
                        val existsInContacts =
                            deviceContactDao.countByPhone(phoneE164) > 0

                        if (existsInContacts) {
                            return@withContext CallDecision(
                                allow = true,
                                reason = CallDecisionReason.ALLOWED_BY_CONTACTS
                            )
                        }
                    }

                    if (settings.blockUnknown) {
                        CallDecision(false, CallDecisionReason.BLOCKED_NOT_IN_WHITELIST)
                    } else {
                        CallDecision(true, CallDecisionReason.ALLOWED_UNKNOWN_DISABLED)
                    }
                }
            }
        }
    }

    suspend fun shouldAllowCall(
        phone: String?,
        settings: AppSettings,
        isLicensed: Boolean,
        defaultRegion: String?
    ): Boolean {
        return evaluate(
            phone = phone,
            settings = settings,
            isLicensed = isLicensed,
            defaultRegion = defaultRegion
        ).allow
    }
}