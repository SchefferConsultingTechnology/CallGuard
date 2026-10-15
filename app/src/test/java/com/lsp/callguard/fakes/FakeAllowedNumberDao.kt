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

    override suspend fun updateLabel(id: String, label: String, updatedAt: Long) {
        state.value = state.value.map {
            if (it.id == id) it.copy(label = label, updatedAt = updatedAt) else it
        }
    }

    override suspend fun deleteByPhone(phoneE164: String) {
        state.value = state.value.filterNot { it.phoneE164 == phoneE164 }
    }

    override suspend fun getAll(): List<AllowedNumberEntity> = state.value

    override suspend fun updatePhone(id: String, phoneE164: String, updatedAt: Long) {
        state.value = state.value.map {
            if (it.id == id) it.copy(phoneE164 = phoneE164, updatedAt = updatedAt) else it
        }
    }

    val current: List<AllowedNumberEntity>
        get() = state.value
}
