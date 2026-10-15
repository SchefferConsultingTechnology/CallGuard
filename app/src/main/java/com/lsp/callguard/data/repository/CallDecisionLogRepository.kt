package com.lsp.callguard.data.repository

import com.lsp.callguard.data.local.dao.CallDecisionLogDao
import com.lsp.callguard.data.local.entity.CallDecisionLogEntity
import com.lsp.callguard.domain.billing.historyCutoff
import com.lsp.callguard.domain.engine.CallDecision
import com.lsp.callguard.domain.model.CallDecisionLog
import com.lsp.callguard.domain.model.Tier
import com.lsp.callguard.domain.phone.PhoneNormalizationResult
import com.lsp.callguard.domain.phone.PhoneNormalizer
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CallDecisionLogRepository(
    private val dao: CallDecisionLogDao,
    private val normalizer: PhoneNormalizer
) {
    fun observeLatestLogs(): Flow<List<CallDecisionLog>> {
        return dao.observeLatest().map { list ->
            list.map {
                CallDecisionLog(
                    id = it.id,
                    phone = it.phone,
                    normalizedPhone = it.normalizedPhone,
                    allowed = it.allowed,
                    reason = it.reason,
                    createdAt = it.createdAt,
                )
            }
        }
    }

    suspend fun logDecision(
        originalPhone: String?,
        decision: CallDecision,
        defaultRegion: String?
    ) {
        val normalized = when (val result = normalizer.normalize(originalPhone, defaultRegion)) {
            is PhoneNormalizationResult.Valid -> result.phoneE164
            is PhoneNormalizationResult.Invalid -> null
        }

        dao.insert(
            CallDecisionLogEntity(
                id = UUID.randomUUID().toString(),
                phone = originalPhone,
                normalizedPhone = normalized,
                allowed = decision.allow,
                reason = decision.reason.name,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    /** Deletes history older than the tier keeps (constitution II: defined retention). */
    suspend fun purge(tier: Tier, nowMillis: Long = System.currentTimeMillis()) {
        dao.deleteOlderThan(historyCutoff(nowMillis, tier))
    }

    suspend fun clear() {
        dao.clear()
    }
}

