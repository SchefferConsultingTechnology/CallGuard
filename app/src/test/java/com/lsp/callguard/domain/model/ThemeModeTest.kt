package com.lsp.callguard.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `stored values map to modes`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("system"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromStorage("light"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage("dark"))
    }

    @Test
    fun `missing or unknown values follow the system`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage(""))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("blue"))
    }

    @Test
    fun `storage value round trips`() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromStorage(mode.storageValue))
        }
    }
}
