package com.lsp.callguard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lsp.callguard.data.local.entity.CallDecisionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDecisionLogDao {

    @Insert
    suspend fun insert(entity: CallDecisionLogEntity)

    @Query("SELECT * FROM call_decision_logs ORDER BY createdAt DESC LIMIT :limit")
    fun observeLatest(limit: Int = 100): Flow<List<CallDecisionLogEntity>>

    @Query("DELETE FROM call_decision_logs")
    suspend fun clear()
}

