package com.lsp.callguard.ui.screen.settings

import androidx.annotation.StringRes
import com.lsp.callguard.R
import com.lsp.callguard.domain.model.ThemeMode
import com.lsp.callguard.domain.model.Tier

/** Argument of a summary: another string resource, or literal text (e.g. a language name). */
sealed interface SummaryArg {
    data class Res(@param:StringRes val id: Int) : SummaryArg
    data class Text(val value: String) : SummaryArg
}

/** One line under a category in the Ajustes list, resolved by the UI. */
data class Summary(@param:StringRes val text: Int, val args: List<SummaryArg> = emptyList())

/** Current state of each category, shown in the Ajustes list (data-model, feature 007). */
fun settingsSummaries(state: SettingsUiState): Map<SettingsCategory, Summary> {
    val settings = state.appSettings

    val plan = when (state.tier) {
        Tier.FREE -> R.string.plan_tier_free
        Tier.PLUS -> R.string.plan_tier_plus
        Tier.PRO -> R.string.plan_tier_pro
    }

    val protection = when {
        settings.blockUnknown && settings.blockPrivateNumbers -> R.string.summary_block_both
        settings.blockUnknown -> R.string.summary_block_unknown
        settings.blockPrivateNumbers -> R.string.summary_block_private
        else -> R.string.summary_block_none
    }

    val contacts = when {
        !state.hasPaidPlan -> R.string.summary_contacts_locked
        settings.useContactsAutomatically -> R.string.summary_on
        else -> R.string.summary_off
    }

    val theme = when (state.themeMode) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    }
    val language = state.language?.let { SummaryArg.Text(it.title) }
        ?: SummaryArg.Res(R.string.summary_language_device)

    return mapOf(
        SettingsCategory.PLAN to Summary(plan),
        SettingsCategory.PROTECTION to Summary(protection),
        SettingsCategory.CONTACTS to Summary(contacts),
        SettingsCategory.BACKUP to Summary(R.string.summary_backup),
        SettingsCategory.APPEARANCE to Summary(R.string.summary_appearance, listOf(SummaryArg.Res(theme), language)),
        SettingsCategory.PRIVACY to Summary(R.string.summary_privacy)
    )
}
