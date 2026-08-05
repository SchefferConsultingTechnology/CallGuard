package com.lsp.callguard.domain.phone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNormalizerTest {

    @Test
    fun `valid E164 number is recognized as valid`() {
        val result = PhoneNormalizer.normalize("+15551234567")

        assertTrue(result is PhoneNormalizationResult.Valid)
        assertEquals("+15551234567", (result as PhoneNormalizationResult.Valid).phoneE164)
    }

    @Test
    fun `spaces dashes and parentheses are stripped before validation`() {
        val result = PhoneNormalizer.normalize("+1 (555) 123-4567")

        assertTrue(result is PhoneNormalizationResult.Valid)
        assertEquals("+15551234567", (result as PhoneNormalizationResult.Valid).phoneE164)
    }

    @Test
    fun `missing country code is invalid`() {
        val result = PhoneNormalizer.normalize("5551234567")

        assertTrue(result is PhoneNormalizationResult.Invalid)
        assertEquals(
            PhoneNormalizationError.MISSING_COUNTRY_CODE,
            (result as PhoneNormalizationResult.Invalid).reason
        )
    }

    @Test
    fun `blank input is invalid`() {
        val result = PhoneNormalizer.normalize("   ")

        assertTrue(result is PhoneNormalizationResult.Invalid)
        assertEquals(
            PhoneNormalizationError.EMPTY,
            (result as PhoneNormalizationResult.Invalid).reason
        )
    }

    @Test
    fun `null input is invalid`() {
        val result = PhoneNormalizer.normalize(null)

        assertTrue(result is PhoneNormalizationResult.Invalid)
        assertEquals(
            PhoneNormalizationError.EMPTY,
            (result as PhoneNormalizationResult.Invalid).reason
        )
    }

    @Test
    fun `non E164 shape is invalid`() {
        val result = PhoneNormalizer.normalize("+0123")

        assertTrue(result is PhoneNormalizationResult.Invalid)
        assertEquals(
            PhoneNormalizationError.INVALID_E164,
            (result as PhoneNormalizationResult.Invalid).reason
        )
    }

    @Test
    fun `isValidE164 mirrors normalize result`() {
        assertTrue(PhoneNormalizer.isValidE164("+15551234567"))
        assertTrue(!PhoneNormalizer.isValidE164("5551234567"))
    }
}
