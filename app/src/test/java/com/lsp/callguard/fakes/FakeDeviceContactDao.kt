package com.lsp.callguard.fakes

import com.lsp.callguard.data.local.dao.DeviceContactDao
import com.lsp.callguard.data.local.entity.DeviceContactEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDeviceContactDao(
    initialContacts: List<DeviceContactEntity> = emptyList()
) : DeviceContactDao {

    private val state = MutableStateFlow(initialContacts)

    override fun observeAll(): Flow<List<DeviceContactEntity>> = state

    override fun observeCount(): Flow<Int> = state.map { it.size }

    override suspend fun countByPhone(phoneE164: String): Int {
        return state.value.count { it.phoneE164 == phoneE164 }
    }

    override suspend fun insertAll(contacts: List<DeviceContactEntity>) {
        state.value = state.value + contacts
    }

    override suspend fun clear() {
        clearCalls++
        state.value = emptyList()
    }

    /** Mirrors the Room @Transaction: readers never observe an intermediate empty cache. */
    override suspend fun replaceAll(contacts: List<DeviceContactEntity>) {
        replaceAllCalls++
        state.value = contacts
    }

    /** Standalone clear() calls (outside replaceAll). */
    var clearCalls = 0
        private set

    var replaceAllCalls = 0
        private set

    val current: List<DeviceContactEntity>
        get() = state.value
}
