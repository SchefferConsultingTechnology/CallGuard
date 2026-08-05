package com.lsp.callguard.ui.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Whitelist : Routes("whitelist")
    data object Onboarding: Routes("onboarding")
    data object Settings : Routes("settings")
    data object Language : Routes("language")

    data object Paywall : Routes("paywall")

    data object ProtectionSetup : Routes("protection_setup")

    data object CallHistory : Routes("call_history")

    data object ContactsConsent : Routes("contacts_consent")

    data object ContactsImport : Routes("contacts_import")

    data object Terms : Routes("terms")

    data object PrivacyPolicy : Routes("privacy_policy")

}

