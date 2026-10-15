package com.lsp.callguard.ui.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Whitelist : Routes("whitelist")
    data object Onboarding: Routes("onboarding")
    /** Start of the Ajustes tab graph (category list). */
    data object Settings : Routes("settings")

    /** Ajustes tab: the list plus one page per category (feature 007). */
    data object SettingsGraph : Routes("settings_graph")

    data object SettingsCategoryPage : Routes("settings/{category}") {
        const val ARG_CATEGORY = "category"

        fun create(category: String) = "settings/$category"
    }
    data object Language : Routes("language")

    data object Paywall : Routes("paywall?firstRun={firstRun}") {
        const val ARG_FIRST_RUN = "firstRun"

        fun create(firstRun: Boolean) = "paywall?$ARG_FIRST_RUN=$firstRun"
    }

    data object ProtectionSetup : Routes("protection_setup")

    data object CallHistory : Routes("call_history")

    data object ContactsConsent : Routes("contacts_consent")

    data object ContactsImport : Routes("contacts_import")

    data object Terms : Routes("terms")

    data object PrivacyPolicy : Routes("privacy_policy")

}

