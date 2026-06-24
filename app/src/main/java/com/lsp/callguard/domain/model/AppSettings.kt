package com.lsp.callguard.domain.model

data class AppSettings(
    val blockUnknown: Boolean = true,
    val blockPrivateNumbers: Boolean = true,
    val useContactsAutomatically: Boolean = false,
    val isProtectionEnabled: Boolean = false,
    val hasAcceptedContactsConsent: Boolean = false
)