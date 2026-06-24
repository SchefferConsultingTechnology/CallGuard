package com.lsp.callguard.domain.model

data class SubscriptionState(
    val isSubscribed: Boolean = false,
    val planName: String = "Free"
)