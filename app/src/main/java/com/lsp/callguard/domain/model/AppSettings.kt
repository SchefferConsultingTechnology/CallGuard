package com.lsp.callguard.domain.model

data class AppSettings(
    val blockUnknown: Boolean = true,
    val allowContacts: Boolean = true,
    val allowWhitelist: Boolean = true,
    val blockPrivateNumbers: Boolean = true
)

