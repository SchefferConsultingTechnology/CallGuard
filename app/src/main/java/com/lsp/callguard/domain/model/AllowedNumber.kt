package com.lsp.callguard.domain.model

data class AllowedNumber(
    val id: String,
    val phoneNumber: String,
    val label: String? = null
)
