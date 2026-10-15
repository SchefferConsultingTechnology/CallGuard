package com.lsp.callguard.domain.model

/** What each tier unlocks; the only place limits are defined. */
data class PlanFeatures(
    /** Null = unlimited. */
    val whitelistLimit: Int?,
    val contactsAllowed: Boolean,
    val historyRetentionDays: Int,
    val driveBackup: Boolean
) {
    companion object {
        fun of(tier: Tier): PlanFeatures = when (tier) {
            Tier.FREE -> PlanFeatures(
                whitelistLimit = 5,
                contactsAllowed = false,
                historyRetentionDays = 7,
                driveBackup = false
            )
            Tier.PLUS -> PlanFeatures(
                whitelistLimit = null,
                contactsAllowed = true,
                historyRetentionDays = 90,
                driveBackup = false
            )
            Tier.PRO -> PlanFeatures(
                whitelistLimit = null,
                contactsAllowed = true,
                historyRetentionDays = 365,
                driveBackup = true
            )
        }
    }
}
