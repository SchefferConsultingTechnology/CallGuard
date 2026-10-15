package com.lsp.callguard.domain.billing

/** Library-free view of a store purchase, so plan rules stay testable on the JVM. */
data class PurchaseSnapshot(
    val productIds: List<String>,
    val state: PurchaseState,
    /** False for one-time purchases and for subscriptions the user canceled. */
    val isAutoRenewing: Boolean,
    val purchaseToken: String
)

enum class PurchaseState {
    PURCHASED,
    PENDING,
    UNSPECIFIED
}
