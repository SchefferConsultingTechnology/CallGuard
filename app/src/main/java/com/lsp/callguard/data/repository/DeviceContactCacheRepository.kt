package com.lsp.callguard.data.repository

import com.lsp.callguard.data.local.dao.DeviceContactDao
import com.lsp.callguard.data.local.entity.DeviceContactEntity
import com.lsp.callguard.domain.model.DeviceContact

class DeviceContactCacheRepository(
    private val dao: DeviceContactDao
) {
    suspend fun replaceAll(contacts: List<DeviceContact>) {
        val now = System.currentTimeMillis()

        dao.clear()

        dao.insertAll(
            contacts.map {
                DeviceContactEntity(
                    id = "${it.id}_${it.phoneE164}",
                    displayName = it.displayName,
                    phoneE164 = it.phoneE164,
                    importedAt = now
                )
            }
        )
    }

    suspend fun countByPhone(phoneE164: String): Int {
        return dao.countByPhone(phoneE164)
    }
}
