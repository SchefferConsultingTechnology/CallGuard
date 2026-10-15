package com.lsp.callguard.fakes

import com.lsp.callguard.data.local.dao.CallDecisionLogDao
import com.lsp.callguard.data.local.entity.CallDecisionLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCallDecisionLogDao(
    initialLogs: List<CallDecisionLogEntity> = emptyList()
) : CallDecisionLogDao {

    private val state = MutableStateFlow(initialLogs)

    override suspend fun insert(entity: CallDecisionLogEntity) {
        state.value = state.value + entity
    }

    override fun observeLatest(): Flow<List<CallDecisionLogEntity>> =
        state.map { logs -> logs.sortedByDescending { it.createdAt } }

    override suspend fun deleteOlderThan(cutoff: Long) {
        state.value = state.value.filter { it.createdAt >= cutoff }
    }

    override suspend fun clear() {
        state.value = emptyList()
    }
}
