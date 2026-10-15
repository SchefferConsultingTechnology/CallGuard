package com.lsp.callguard.core.language

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class AppLanguageTest {

    @Test
    fun `portuguese variants map to PT_BR`() {
        assertEquals(AppLanguage.PT_BR, AppLanguage.forLocale(Locale("pt", "BR")))
        assertEquals(AppLanguage.PT_BR, AppLanguage.forLocale(Locale("pt", "PT")))
    }

    @Test
    fun `spanish variants map to ES_ES`() {
        assertEquals(AppLanguage.ES_ES, AppLanguage.forLocale(Locale("es", "ES")))
        assertEquals(AppLanguage.ES_ES, AppLanguage.forLocale(Locale("es", "MX")))
    }

    @Test
    fun `english variants map to EN_US`() {
        assertEquals(AppLanguage.EN_US, AppLanguage.forLocale(Locale.US))
        assertEquals(AppLanguage.EN_US, AppLanguage.forLocale(Locale.UK))
    }

    @Test
    fun `unsupported language maps to null`() {
        assertNull(AppLanguage.forLocale(Locale.FRENCH))
    }
}
