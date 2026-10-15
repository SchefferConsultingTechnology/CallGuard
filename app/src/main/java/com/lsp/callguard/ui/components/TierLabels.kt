package com.lsp.callguard.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lsp.callguard.R
import com.lsp.callguard.domain.model.Tier

@Composable
fun tierDisplayName(tier: Tier): String = when (tier) {
    Tier.FREE -> stringResource(R.string.plan_tier_free)
    Tier.PLUS -> stringResource(R.string.plan_tier_plus)
    Tier.PRO -> stringResource(R.string.plan_tier_pro)
}
