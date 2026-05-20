package com.lsp.callguard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_decision_logs")
data class CallDecisionLogEntity(
    @PrimaryKey val id: String,
    val phone: String?,
    val normalizedPhone: String?,
    val allowed: Boolean,
    val reason: String,
    val createdAt: Long
)

