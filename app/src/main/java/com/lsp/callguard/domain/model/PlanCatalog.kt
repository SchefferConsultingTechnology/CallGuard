package com.lsp.callguard.domain.model

/** Play Console identifiers. They must match the store exactly and can never be renamed there. */
object PlanCatalog {
    const val PLUS = "callguard_plus"
    const val PLUS_LIFETIME = "callguard_plus_lifetime"
    const val PRO = "callguard_pro"

    const val BASE_MONTHLY = "monthly"
    const val BASE_ANNUAL = "annual"
    const val OFFER_ANNUAL_TRIAL = "annual-trial-7d"

    /** Pro is only sold once Drive backup (feature 005) ships; the offer must not sell what does not exist. */
    const val PRO_ON_SALE = false

    fun tierOf(productId: String): Tier? = when (productId) {
        PRO -> Tier.PRO
        PLUS, PLUS_LIFETIME -> Tier.PLUS
        else -> null
    }
}
