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
        state.value = emptyList()
    }
}
