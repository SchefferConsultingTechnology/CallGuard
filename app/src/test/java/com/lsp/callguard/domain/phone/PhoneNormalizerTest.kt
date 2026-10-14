package com.lsp.callguard.domain.phone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mirrors the `PhoneNormalizer` table in specs/001-phone-normalization/contracts/phone-normalizer.md. */
class PhoneNormalizerTest {

    private val normalizer = PhoneNormalizer()

    private fun assertValid(input: String?, region: String?, expected: String) {
        val result = normalizer.normalize(input, region)
        assertTrue("$input ($region) should be valid, was $result", result is PhoneNormalizationResult.Valid)
        assertEquals(expected, (result as PhoneNormalizationResult.Valid).phoneE164)
    }

    private fun assertInvalid(input: String?, region: String?, reason: PhoneNormalizationError) {
        val result = normalizer.normalize(input, region)
        assertTrue("$input ($region) should be invalid, was $result", result is PhoneNormalizationResult.Invalid)
        assertEquals(reason, (result as PhoneNormalizationResult.Invalid).reason)
    }

    // Brazilian national formats (contract #1–#8)

    @Test fun `01 national mobile`() = assertValid("11987654321", "BR", "+5511987654321")
    @Test fun `02 trunk prefix`() = assertValid("011987654321", "BR", "+5511987654321")
    @Test fun `03 carrier code`() = assertValid("01511987654321", "BR", "+5511987654321")
    @Test fun `04 0xx prefix`() = assertValid("0xx11987654321", "BR", "+5511987654321")
    @Test fun `05 punctuation`() = assertValid("(11) 98765-4321", "BR", "+5511987654321")
    @Test fun `06 dots`() = assertValid("11.98765.4321", "BR", "+5511987654321")
    @Test fun `07 international with spaces`() = assertValid("+55 11 98765-4321", "BR", "+5511987654321")
    @Test fun `08 country code without plus`() = assertValid("5511987654321", "BR", "+5511987654321")

    // International prefix 00 (contract #9, #27)

    @Test fun `09 00 international prefix`() = assertValid("005511987654321", "BR", "+5511987654321")
    @Test fun `27 00 with carrier code to foreign number`() =
        assertValid("0015 41 44 668 18 00", "BR", "+41446681800")

    // Legacy 8-digit Brazilian mobiles (contract #10–#12)

    @Test fun `10 legacy 8 digit mobile gets ninth digit`() = assertValid("(11) 8765-4321", "BR", "+5511987654321")
    @Test fun `11 legacy E164 gets ninth digit in any region`() {
        assertValid("+551187654321", "BR", "+5511987654321")
        assertValid("+551187654321", "PT", "+5511987654321")
        assertValid("+551187654321", null, "+5511987654321")
    }
    @Test fun `12 legacy mobile in another area code`() = assertValid("(81) 8765-4321", "BR", "+5581987654321")

    // Other Brazilian number types (contract #13–#15)

    @Test fun `13 landline is untouched`() = assertValid("(11) 3333-4444", "BR", "+551133334444")
    @Test fun `14 toll free`() = assertValid("0800 123 4567", "BR", "+558001234567")
    @Test fun `15 shared cost`() = assertValid("4004-1234", "BR", "+5540041234")

    // Unparseable (contract #16–#21)

    @Test fun `16 mobile without area code`() = assertInvalid("98765-4321", "BR", PhoneNormalizationError.NOT_VALID)
    @Test fun `17 short code`() = assertInvalid("*144", "BR", PhoneNormalizationError.NOT_VALID)
    @Test fun `18 too short`() = assertInvalid("12345", "BR", PhoneNormalizationError.NOT_VALID)
    @Test fun `19 letters`() = assertInvalid("abc", "BR", PhoneNormalizationError.NOT_A_NUMBER)
    @Test fun `20 empty inputs`() {
        assertInvalid("", "BR", PhoneNormalizationError.EMPTY)
        assertInvalid(null, "BR", PhoneNormalizationError.EMPTY)
        assertInvalid("   ", "BR", PhoneNormalizationError.EMPTY)
    }
    @Test fun `21 national number without region`() =
        assertInvalid("11987654321", null, PhoneNormalizationError.NO_DEFAULT_REGION)

    // International numbers (contract #22–#26, #28)

    @Test fun `22 international without region`() = assertValid("+5511987654321", null, "+5511987654321")
    @Test fun `23 portuguese number from brazil`() = assertValid("+351 912 345 678", "BR", "+351912345678")
    @Test fun `24 portuguese national`() = assertValid("912345678", "PT", "+351912345678")
    @Test fun `25 spanish national`() = assertValid("612 34 56 78", "ES", "+34612345678")
    @Test fun `26 us national`() = assertValid("(650) 555-1212", "US", "+16505551212")
    @Test fun `28 us international from brazil`() = assertValid("+1 650 555 1212", "BR", "+16505551212")

    // Internal failure (contract #29)

    @Test
    fun `29 unexpected exception becomes INTERNAL_ERROR`() {
        val failing = PhoneNormalizer(parse = { _, _ -> throw IllegalStateException("boom") })
        val result = failing.normalize("11987654321", "BR")
        assertEquals(
            PhoneNormalizationResult.Invalid(PhoneNormalizationError.INTERNAL_ERROR),
            result
        )
    }

    // General rules

    @Test
    fun `normalization is idempotent for any region`() {
        val canonical = listOf("+5511987654321", "+551133334444", "+351912345678", "+16505551212")
        for (phone in canonical) {
            for (region in listOf("BR", "PT", null)) {
                assertValid(phone, region, phone)
            }
        }
    }

    @Test
    fun `never throws on odd input`() {
        val odd = listOf(
            "+", "++55", "0", "00", "0000000000000000000000000000", "9".repeat(500),
            "+55 (11) ###", "tel:+5511987654321", "\u0000\u0001", "١١٩٨٧٦٥٤٣٢١"
        )
        for (input in odd) {
            normalizer.normalize(input, "BR")
            normalizer.normalize(input, null)
        }
    }

    @Test
    fun `valid result carries the number region`() {
        val result = normalizer.normalize("11987654321", "BR") as PhoneNormalizationResult.Valid
        assertEquals("BR", result.regionCode)
    }
}
