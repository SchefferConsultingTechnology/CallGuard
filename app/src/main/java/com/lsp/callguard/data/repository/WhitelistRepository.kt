package com.lsp.callguard.data.repository

import com.lsp.callguard.data.local.dao.AllowedNumberDao
import com.lsp.callguard.data.local.entity.AllowedNumberEntity
import com.lsp.callguard.domain.model.AllowedNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WhitelistRepository(
    private val dao: AllowedNumberDao,
    private val fallbackLabel: String
) {
    fun observeAllowedNumbers(): Flow<List<AllowedNumber>> {
        return dao.observeAll().map { list ->
            list.map { entity ->
                AllowedNumber(
                    id = entity.id,
                    label = entity.label,
                    phoneE164 = entity.phoneE164
                )
            }
        }
    }

    fun observeCount(): Flow<Int> {
        return dao.observeCount()
    }

    suspend fun addNumber(
        label: String,
        phoneE164: String
    ): AddAllowedNumberResult {
        val exists = dao.countByPhone(phoneE164) > 0

        if (exists) {
            return AddAllowedNumberResult.AlreadyExists
        }

        val now = System.currentTimeMillis()

        dao.insert(
            AllowedNumberEntity(
                id = UUID.randomUUID().toString(),
                label = label.ifBlank { fallbackLabel },
                phoneE164 = phoneE164,
                createdAt = now,
                updatedAt = now
            )
        )

        return AddAllowedNumberResult.Success
    }

    suspend fun deleteNumber(id: String) {
        dao.deleteById(id)
    }
}

sealed class AddAllowedNumberResult {
    data object Success : AddAllowedNumberResult()
    data object AlreadyExists : AddAllowedNumberResult()
}

