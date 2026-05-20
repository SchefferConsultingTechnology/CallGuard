package com.lsp.callguard.data.repository

import com.lsp.callguard.data.local.dao.CallDecisionLogDao
import com.lsp.callguard.data.local.entity.CallDecisionLogEntity
import com.lsp.callguard.domain.engine.CallDecision
import com.lsp.callguard.domain.model.CallDecisionLog
import com.lsp.callguard.domain.phone.PhoneNormalizationResult
import com.lsp.callguard.domain.phone.PhoneNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class CallDecisionLogRepository(
    private val dao: CallDecisionLogDao
) {
    fun observeLatestLogs(limit: Int = 100): Flow<List<CallDecisionLog>> {
        return dao.observeLatest(limit).map { list ->
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
        decision: CallDecision
    ) {
        val normalized = when (val result = PhoneNormalizer.normalize(originalPhone)) {
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

    suspend fun clear() {
        dao.clear()
    }
}

