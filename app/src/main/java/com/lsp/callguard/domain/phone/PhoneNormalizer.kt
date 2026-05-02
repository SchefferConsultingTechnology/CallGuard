package com.lsp.callguard.domain.phone

object PhoneNormalizer {

    private val e164Regex = Regex("^\\+[1-9]\\d{7,14}$")

    fun normalize(input: String?): PhoneNormalizationResult {
        val raw = input?.trim().orEmpty()

        if (raw.isBlank()) {
            return PhoneNormalizationResult.Invalid(
                reason = PhoneNormalizationError.EMPTY
            )
        }

        val compact = raw
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")

        if (!compact.startsWith("+")) {
            return PhoneNormalizationResult.Invalid(
                reason = PhoneNormalizationError.MISSING_COUNTRY_CODE
            )
        }

        if (!e164Regex.matches(compact)) {
            return PhoneNormalizationResult.Invalid(
                reason = PhoneNormalizationError.INVALID_E164
            )
        }

        return PhoneNormalizationResult.Valid(
            phoneE164 = compact
        )
    }

    fun isValidE164(input: String?): Boolean {
        return normalize(input) is PhoneNormalizationResult.Valid
    }
}

sealed class PhoneNormalizationResult {
    data class Valid(
        val phoneE164: String
    ) : PhoneNormalizationResult()

    data class Invalid(
        val reason: PhoneNormalizationError
    ) : PhoneNormalizationResult()
}

enum class PhoneNormalizationError {
    EMPTY,
    MISSING_COUNTRY_CODE,
    INVALID_E164
}
