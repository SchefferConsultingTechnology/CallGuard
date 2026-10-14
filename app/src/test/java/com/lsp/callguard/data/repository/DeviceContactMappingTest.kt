package com.lsp.callguard.data.repository

import com.lsp.callguard.domain.phone.PhoneNormalizer
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceContactMappingTest {

    private val rows = listOf(
        RawContactRow(id = "1", displayName = "Mãe", rawPhone = "(11) 98765-4321"),
        RawContactRow(id = "1", displayName = "Mãe", rawPhone = "11987654321"),
        RawContactRow(id = "2", displayName = "Mãe (trabalho)", rawPhone = "+55 11 98765-4321"),
        RawContactRow(id = "3", displayName = "Tio", rawPhone = "(11) 8765-4322"),
        RawContactRow(id = "4", displayName = "Operadora", rawPhone = "*144"),
        RawContactRow(id = "5", displayName = "", rawPhone = "+351912345678")
    )

    private val contacts = mapToDeviceContacts(
        rows = rows,
        normalizer = PhoneNormalizer(),
        defaultRegion = "BR",
        fallbackName = "No name"
    )

    @Test
    fun `national formats are imported and deduplicated by canonical number`() {
        assertEquals(
            listOf("+5511987654321", "+5511987654322", "+351912345678"),
            contacts.map { it.phoneE164 }
        )
    }

    @Test
    fun `first row wins when the same number appears more than once`() {
        assertEquals("Mãe", contacts.first().displayName)
        assertEquals("1", contacts.first().id)
    }

    @Test
    fun `blank name uses the fallback`() {
        assertEquals("No name", contacts.last().displayName)
    }

    @Test
    fun `unparseable numbers are skipped`() {
        assertEquals(false, contacts.any { it.id == "4" })
    }
}
