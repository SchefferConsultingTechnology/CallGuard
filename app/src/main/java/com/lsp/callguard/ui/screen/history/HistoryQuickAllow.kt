package com.lsp.callguard.ui.screen.history

/** What a call-history entry offers about the whitelist. */
enum class QuickAllowAction {
    /** Blocked and identifiable: can be added to the whitelist. */
    ADD,
    IN_WHITELIST,
    NONE
}

/**
 * @param canonical the entry's number in the single identity of feature 001, or null when it was
 * hidden or could not be interpreted (those can never be whitelisted).
 */
fun quickAllowAction(allowed: Boolean, canonical: String?, inWhitelist: Set<String>): QuickAllowAction {
    if (canonical == null) return QuickAllowAction.NONE
    if (canonical in inWhitelist) return QuickAllowAction.IN_WHITELIST
    return if (allowed) QuickAllowAction.NONE else QuickAllowAction.ADD
}
