package com.lsp.callguard.domain.model

data class CallDecisionLog(
    val id: String,
    val phone: String?,
    val normalizedPhone: String?,
    val allowed: Boolean,
    val reason: String,
    val createdAt: Long
)

