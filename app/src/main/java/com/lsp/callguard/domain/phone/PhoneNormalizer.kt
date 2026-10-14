package com.lsp.callguard.domain.phone

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType
import com.google.i18n.phonenumbers.Phonenumber
import kotlin.coroutines.cancellation.CancellationException

/**
 * Single source of truth for phone identity (constitution, Principle III): every entry point
 * (incoming calls, contacts import, manual whitelist entry) turns raw text into the same E.164 key.
 *
 * Never throws. An [PhoneNormalizationResult.Invalid] result is never a reason to block a call by
 * itself; see `CallDecisionEngine`.
 */
class PhoneNormalizer(
    private val util: PhoneNumberUtil = PhoneNumberUtil.getInstance(),
    private val parse: (String, String?) -> Phonenumber.PhoneNumber = { number, region ->
        util.parse(number, region)
    }
) {

    fun normalize(input: String?, defaultRegion: String?): PhoneNormalizationResult {
        val raw = input?.trim().orEmpty()

        if (raw.isEmpty()) {
            return PhoneNormalizationResult.Invalid(PhoneNormalizationError.EMPTY)
        }

        if (raw.none { it.isDigit() }) {
            return PhoneNormalizationResult.Invalid(PhoneNormalizationError.NOT_A_NUMBER)
        }

        return try {
            normalizeParsable(raw, defaultRegion)
        } catch (e: NumberParseException) {
            PhoneNormalizationResult.Invalid(PhoneNormalizationError.NOT_VALID)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            PhoneNormalizationResult.Invalid(PhoneNormalizationError.INTERNAL_ERROR)
        }
    }

    private fun normalizeParsable(raw: String, defaultRegion: String?): PhoneNormalizationResult {
        val isInternational = raw.startsWith("+")

        if (!isInternational) {
            internationalPrefixCandidates(raw, defaultRegion).forEach { candidate ->
                val number = parseOrNull(candidate) ?: return@forEach
                accept(number, international = true)?.let { return it }
            }

            if (defaultRegion == null) {
                return PhoneNormalizationResult.Invalid(PhoneNormalizationError.NO_DEFAULT_REGION)
            }
        }

        val number = parse(raw, defaultRegion)

        return accept(number, international = isInternational)
            ?: PhoneNormalizationResult.Invalid(PhoneNormalizationError.NOT_VALID)
    }

    /**
     * Research R4: "00" dialed as international prefix. In Brazil it is followed by a two-digit
     * carrier code (e.g. 0015 41 ...), which libphonenumber does not strip on its own.
     */
    private fun internationalPrefixCandidates(raw: String, defaultRegion: String?): List<String> {
        val digits = raw.filter { it in '0'..'9' }

        if (!digits.startsWith("00") || digits.length <= 2) {
            return emptyList()
        }

        val rest = digits.drop(2)

        return buildList {
            add("+$rest")
            if (defaultRegion == BRAZIL_REGION && rest.length > 2) {
                add("+${rest.drop(2)}")
            }
        }
    }

    private fun parseOrNull(candidate: String): Phonenumber.PhoneNumber? {
        return try {
            parse(candidate, null)
        } catch (e: NumberParseException) {
            null
        }
    }

    /** Research R2 (validity criterion) and R3 (legacy 8-digit Brazilian mobiles). */
    private fun accept(
        number: Phonenumber.PhoneNumber,
        international: Boolean
    ): PhoneNormalizationResult.Valid? {
        if (util.isValidNumber(number)) {
            return valid(number)
        }

        withBrazilianNinthDigit(number)?.let { return valid(it) }

        if (international && util.isPossibleNumber(number)) {
            return valid(number)
        }

        return null
    }

    private fun withBrazilianNinthDigit(number: Phonenumber.PhoneNumber): Phonenumber.PhoneNumber? {
        if (number.countryCode != BRAZIL_COUNTRY_CODE) return null

        val nationalNumber = util.getNationalSignificantNumber(number)
        if (nationalNumber.length != 10 || nationalNumber[2] !in '6'..'9') return null

        val upgraded = Phonenumber.PhoneNumber()
            .setCountryCode(BRAZIL_COUNTRY_CODE)
            .setNationalNumber("${nationalNumber.take(2)}9${nationalNumber.drop(2)}".toLong())

        val isMobile = util.isValidNumber(upgraded) &&
            util.getNumberType(upgraded) == PhoneNumberType.MOBILE

        return upgraded.takeIf { isMobile }
    }

    private fun valid(number: Phonenumber.PhoneNumber) = PhoneNormalizationResult.Valid(
        phoneE164 = util.format(number, PhoneNumberFormat.E164),
        regionCode = util.getRegionCodeForNumber(number)
    )

    private companion object {
        const val BRAZIL_REGION = "BR"
        const val BRAZIL_COUNTRY_CODE = 55
    }
}

sealed class PhoneNormalizationResult {
    data class Valid(
        val phoneE164: String,
        val regionCode: String? = null
    ) : PhoneNormalizationResult()

    data class Invalid(
        val reason: PhoneNormalizationError
    ) : PhoneNormalizationResult()
}

enum class PhoneNormalizationError {
    /** Blank input: treated as a private/hidden number by the call engine. */
    EMPTY,

    /** No digits at all. */
    NOT_A_NUMBER,

    /** National number but no region is known to interpret it. */
    NO_DEFAULT_REGION,

    /** Parsed but not a valid number for its region. */
    NOT_VALID,

    /** Unexpected failure while interpreting; the call engine always allows (Principle I). */
    INTERNAL_ERROR
}
