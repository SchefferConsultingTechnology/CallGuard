package com.lsp.callguard.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppTabTest {

    @Test
    fun `tab routes map to their tab`() {
        assertEquals(AppTab.HOME, tabFor("home"))
        assertEquals(AppTab.WHITELIST, tabFor("whitelist"))
        assertEquals(AppTab.HISTORY, tabFor("call_history"))
    }

    @Test
    fun `settings list and category pages belong to the settings tab`() {
        assertEquals(AppTab.SETTINGS, tabFor("settings"))
        assertEquals(AppTab.SETTINGS, tabFor("settings/{category}"))
        assertEquals(AppTab.SETTINGS, tabFor("settings/backup"))
    }

    @Test
    fun `task screens hide the bar`() {
        listOf(
            null,
            "onboarding",
            "paywall?firstRun={firstRun}",
            "protection_setup",
            "contacts_consent",
            "contacts_import",
            "terms",
            "privacy_policy",
            "language"
        ).forEach { route -> assertNull(route, tabFor(route)) }
    }
}
