package com.lsp.callguard.domain.engine

import com.lsp.callguard.data.local.dao.AllowedNumberDao
import com.lsp.callguard.domain.model.AppSettings
import com.lsp.callguard.domain.phone.PhoneNormalizationResult
import com.lsp.callguard.domain.phone.PhoneNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallDecisionEngine(
    private val allowedNumberDao: AllowedNumberDao
) {

    suspend fun evaluate(
        phone: String?,
        settings: AppSettings
    ): CallDecision {
        return withContext(Dispatchers.IO) {
            if (phone.isNullOrBlank()) {
                return@withContext if (settings.blockPrivateNumbers) {
                    CallDecision(
                        allow = false,
                        reason = CallDecisionReason.BLOCKED_PRIVATE_NUMBER
                    )
                } else {
                    CallDecision(
                        allow = true,
                        reason = CallDecisionReason.ALLOWED_PRIVATE_NUMBER_DISABLED
                    )
                }
            }

            val normalized = PhoneNormalizer.normalize(phone)

            when (normalized) {
                is PhoneNormalizationResult.Invalid -> {
                    CallDecision(
                        allow = false,
                        reason = CallDecisionReason.INVALID_NUMBER
                    )
                }

                is PhoneNormalizationResult.Valid -> {
                    val existsInWhitelist =
                        allowedNumberDao.countByPhone(normalized.phoneE164) > 0

                    if (existsInWhitelist) {
                        CallDecision(
                            allow = true,
                            reason = CallDecisionReason.ALLOWED_BY_WHITELIST
                        )
                    } else {
                        if (settings.blockUnknown) {
                            CallDecision(
                                allow = false,
                                reason = CallDecisionReason.BLOCKED_NOT_IN_WHITELIST
                            )
                        } else {
                            CallDecision(
                                allow = true,
                                reason = CallDecisionReason.ALLOWED_UNKNOWN_DISABLED
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun shouldAllowCall(
        phone: String?,
        settings: AppSettings
    ): Boolean {
        return evaluate(phone, settings).allow
    }
}