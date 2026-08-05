package com.lsp.callguard.fakes

import com.lsp.callguard.data.local.dao.AllowedNumberDao
import com.lsp.callguard.data.local.entity.AllowedNumberEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAllowedNumberDao(
    initialNumbers: List<AllowedNumberEntity> = emptyList()
) : AllowedNumberDao {

    private val state = MutableStateFlow(initialNumbers)

    override fun observeAll(): Flow<List<AllowedNumberEntity>> = state

    override fun observeCount(): Flow<Int> = state.map { it.size }

    override suspend fun countByPhone(phoneE164: String): Int {
        return state.value.count { it.phoneE164 == phoneE164 }
    }

    override suspend fun insert(entity: AllowedNumberEntity) {
        state.value = state.value + entity
    }

    override suspend fun delete(entity: AllowedNumberEntity) {
        state.value = state.value - entity
    }

    override suspend fun deleteById(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}
