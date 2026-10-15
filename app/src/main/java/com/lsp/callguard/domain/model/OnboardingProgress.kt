package com.lsp.callguard.domain.model

/** First-run progress. Declaration order is the order it advances in; it never goes back. */
enum class OnboardingProgress {
    NOT_STARTED,
    ONBOARDING_DONE,
    COMPLETED
}
