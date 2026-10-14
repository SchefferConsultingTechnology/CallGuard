package com.lsp.callguard.domain.phone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mirrors the `PhoneNumberDisplayFormatter` table in the feature contract. */
class PhoneNumberDisplayFormatterTest {

    private val formatter = PhoneNumberDisplayFormatter()

    @Test
    fun `domestic number uses national format`() {
        assertEquals("(11) 98765-4321", formatter.format("+5511987654321", "BR"))
    }

    @Test
    fun `number from another country uses international format`() {
        assertEquals("+55 11 98765-4321", formatter.format("+5511987654321", "PT"))
        assertEquals("+351 912 345 678", formatter.format("+351912345678", "BR"))
    }

    @Test
    fun `unknown user region uses international format`() {
        assertEquals("+55 11 98765-4321", formatter.format("+5511987654321", null))
    }

    @Test
    fun `unparseable value is returned as is`() {
        assertEquals("not a phone", formatter.format("not a phone", "BR"))
    }

    @Test
    fun `formatOrRaw falls back to the raw value`() {
        assertEquals("12345", formatter.formatOrRaw(null, "12345", "BR"))
        assertNull(formatter.formatOrRaw(null, null, "BR"))
        assertEquals("(11) 98765-4321", formatter.formatOrRaw("+5511987654321", "011987654321", "BR"))
    }

    @Test
    fun `legacy log without canonical number is shown as received`() {
        assertEquals("11987654321", formatter.formatOrRaw(null, "11987654321", "BR"))
    }

    @Test
    fun `example number follows the user region in national format`() {
        val example = formatter.exampleFor("BR")
        assertNotNull(example)
        assertTrue("expected national format, was $example", example!!.startsWith("("))
        assertNull(formatter.exampleFor(null))
    }
}
