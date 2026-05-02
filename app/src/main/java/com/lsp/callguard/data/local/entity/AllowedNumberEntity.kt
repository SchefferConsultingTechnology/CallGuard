package com.lsp.callguard.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "allowed_numbers",
    indices = [
        Index(value = ["phoneE164"], unique = true)
    ]
)
data class AllowedNumberEntity(
    @PrimaryKey
    val id: String,
    val label: String,
    val phoneE164: String,
    val createdAt: Long,
    val updatedAt: Long
)

