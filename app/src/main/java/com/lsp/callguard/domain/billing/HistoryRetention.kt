package com.lsp.callguard.domain.billing

import com.lsp.callguard.domain.model.PlanFeatures
import com.lsp.callguard.domain.model.Tier

private const val DAY_MILLIS = 86_400_000L

/** Oldest call-history timestamp the tier keeps; anything before it is deleted. */
fun historyCutoff(nowMillis: Long, tier: Tier): Long {
    return nowMillis - PlanFeatures.of(tier).historyRetentionDays * DAY_MILLIS
}
