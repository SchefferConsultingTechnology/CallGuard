package com.lsp.callguard.domain.engine

data class CallDecision(
    val allow: Boolean,
    val reason: CallDecisionReason
)

enum class CallDecisionReason {
    ALLOWED_BY_WHITELIST,
    ALLOWED_UNKNOWN_DISABLED,
    BLOCKED_NOT_IN_WHITELIST,
    BLOCKED_PRIVATE_NUMBER,
    ALLOWED_PRIVATE_NUMBER_DISABLED,
    INVALID_NUMBER
}
