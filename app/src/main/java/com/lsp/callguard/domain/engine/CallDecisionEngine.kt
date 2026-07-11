package com.lsp.callguard.domain.engine

import com.lsp.callguard.data.local.dao.AllowedNumberDao
import com.lsp.callguard.data.local.dao.DeviceContactDao
import com.lsp.callguard.domain.model.AppSettings
import com.lsp.callguard.domain.phone.PhoneNormalizationResult
import com.lsp.callguard.domain.phone.PhoneNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallDecisionEngine(
    private val allowedNumberDao: AllowedNumberDao,
    private val deviceContactDao: DeviceContactDao
) {

    suspend fun evaluate(
        phone: String?,
        settings: AppSettings,
        isLicensed: Boolean
    ): CallDecision {
        return withContext(Dispatchers.IO) {
            if (phone.isNullOrBlank()) {
                return@withContext if (settings.blockPrivateNumbers) {
                    CallDecision(false, CallDecisionReason.BLOCKED_PRIVATE_NUMBER)
                } else {
                    CallDecision(true, CallDecisionReason.ALLOWED_PRIVATE_NUMBER_DISABLED)
                }
            }

            val normalized = PhoneNormalizer.normalize(phone)

            when (normalized) {
                is PhoneNormalizationResult.Invalid -> {
                    CallDecision(false, CallDecisionReason.INVALID_NUMBER)
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
        isLicensed: Boolean
    ): Boolean {
        return evaluate(
            phone = phone,
            settings = settings,
            isLicensed = isLicensed
        ).allow
    }
}