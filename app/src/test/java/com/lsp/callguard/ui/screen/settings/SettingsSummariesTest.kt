package com.lsp.callguard.ui.screen.settings

import com.lsp.callguard.R
import com.lsp.callguard.core.language.AppLanguage
import com.lsp.callguard.domain.model.AppSettings
import com.lsp.callguard.domain.model.ThemeMode
import com.lsp.callguard.domain.model.Tier
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsSummariesTest {

    private fun summaryOf(category: SettingsCategory, state: SettingsUiState) =
        settingsSummaries(state).getValue(category)

    private fun state(
        tier: Tier = Tier.FREE,
        blockUnknown: Boolean = true,
        blockPrivate: Boolean = true,
        useContacts: Boolean = false,
        theme: ThemeMode = ThemeMode.SYSTEM,
        language: AppLanguage? = null
    ) = SettingsUiState(
        appSettings = AppSettings(
            blockUnknown = blockUnknown,
            blockPrivateNumbers = blockPrivate,
            useContactsAutomatically = useContacts
        ),
        tier = tier,
        themeMode = theme,
        language = language
    )

    @Test
    fun `every category has a summary`() {
        assertEquals(SettingsCategory.entries.toSet(), settingsSummaries(state()).keys)
    }

    @Test
    fun `plan shows the tier name`() {
        assertEquals(Summary(R.string.plan_tier_free), summaryOf(SettingsCategory.PLAN, state(tier = Tier.FREE)))
        assertEquals(Summary(R.string.plan_tier_plus), summaryOf(SettingsCategory.PLAN, state(tier = Tier.PLUS)))
        assertEquals(Summary(R.string.plan_tier_pro), summaryOf(SettingsCategory.PLAN, state(tier = Tier.PRO)))
    }

    @Test
    fun `protection reflects both switches`() {
        val cases = mapOf(
            (true to true) to R.string.summary_block_both,
            (true to false) to R.string.summary_block_unknown,
            (false to true) to R.string.summary_block_private,
            (false to false) to R.string.summary_block_none
        )
        cases.forEach { (switches, expected) ->
            val actual = summaryOf(SettingsCategory.PROTECTION, state(blockUnknown = switches.first, blockPrivate = switches.second))
            assertEquals(switches.toString(), Summary(expected), actual)
        }
    }

    @Test
    fun `contacts are locked on free and on or off on paid plans`() {
        assertEquals(Summary(R.string.summary_contacts_locked), summaryOf(SettingsCategory.CONTACTS, state(useContacts = true)))
        assertEquals(Summary(R.string.summary_on), summaryOf(SettingsCategory.CONTACTS, state(tier = Tier.PLUS, useContacts = true)))
        assertEquals(Summary(R.string.summary_off), summaryOf(SettingsCategory.CONTACTS, state(tier = Tier.PRO, useContacts = false)))
    }

    @Test
    fun `appearance combines theme and language`() {
        assertEquals(
            Summary(R.string.summary_appearance, listOf(SummaryArg.Res(R.string.theme_dark), SummaryArg.Text("Português"))),
            summaryOf(SettingsCategory.APPEARANCE, state(theme = ThemeMode.DARK, language = AppLanguage.PT_BR))
        )
        assertEquals(
            Summary(R.string.summary_appearance, listOf(SummaryArg.Res(R.string.theme_system), SummaryArg.Res(R.string.summary_language_device))),
            summaryOf(SettingsCategory.APPEARANCE, state(language = null))
        )
    }

    @Test
    fun `backup and privacy are fixed`() {
        assertEquals(Summary(R.string.summary_backup), summaryOf(SettingsCategory.BACKUP, state()))
        assertEquals(Summary(R.string.summary_privacy), summaryOf(SettingsCategory.PRIVACY, state()))
    }
}
