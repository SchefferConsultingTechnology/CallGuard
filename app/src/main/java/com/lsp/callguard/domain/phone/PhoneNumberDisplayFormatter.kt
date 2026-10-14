package com.lsp.callguard.domain.phone

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType

class PhoneNumberDisplayFormatter(
    private val util: PhoneNumberUtil = PhoneNumberUtil.getInstance()
) {

    /** Canonical → readable. National format if number region == userRegion, else international. */
    fun format(phoneE164: String, userRegion: String?): String {
        return try {
            val number = util.parse(phoneE164, null)
            val isDomestic = userRegion != null && util.getRegionCodeForNumber(number) == userRegion
            val format = if (isDomestic) PhoneNumberFormat.NATIONAL else PhoneNumberFormat.INTERNATIONAL

            util.format(number, format)
        } catch (e: NumberParseException) {
            phoneE164
        }
    }

    /** Raw/unparseable → shown exactly as received (FR-010). */
    fun formatOrRaw(normalized: String?, raw: String?, userRegion: String?): String? {
        return normalized?.let { format(it, userRegion) } ?: raw
    }

    /** Example number for the input placeholder, in national format of userRegion. */
    fun exampleFor(userRegion: String?): String? {
        if (userRegion == null) return null

        val example = util.getExampleNumberForType(userRegion, PhoneNumberType.MOBILE) ?: return null

        return util.format(example, PhoneNumberFormat.NATIONAL)
    }
}
