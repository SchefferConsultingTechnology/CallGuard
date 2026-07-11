package com.lsp.callguard.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "device_contacts",
    indices = [
        Index(value = ["phoneE164"], unique = true)
    ]
)
data class DeviceContactEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val phoneE164: String,
    val importedAt: Long
)
