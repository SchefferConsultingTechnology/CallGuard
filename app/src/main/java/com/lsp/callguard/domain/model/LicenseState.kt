package com.lsp.callguard.domain.model

data class LicenseState(
    val isLicensed: Boolean = false,
    val planName: String = "Free"
)