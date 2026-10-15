package com.lsp.callguard.domain.phone

import com.lsp.callguard.domain.model.CallDecisionLog

/**
 * Number of a log entry in the single identity used by call screening (feature 001).
 * Rows written before 001 may lack it; they are re-read with the same rule.
 */
fun canonicalOf(log: CallDecisionLog, region: String?, normalizer: PhoneNormalizer): String? {
    log.normalizedPhone?.let { return it }
    return (normalizer.normalize(log.phone, region) as? PhoneNormalizationResult.Valid)?.phoneE164
}

/** Readable number for a log entry; raw value when it could not be interpreted, null when hidden. */
fun displayPhoneOf(
    log: CallDecisionLog,
    region: String?,
    normalizer: PhoneNormalizer,
    formatter: PhoneNumberDisplayFormatter
): String? = formatter.formatOrRaw(canonicalOf(log, region, normalizer), log.phone, region)
