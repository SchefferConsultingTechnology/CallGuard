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

    /** Volume is bounded by the tier's retention (see deleteOlderThan), not by a row cap. */
    @Query("SELECT * FROM call_decision_logs ORDER BY createdAt DESC")
    fun observeLatest(): Flow<List<CallDecisionLogEntity>>

    @Query("DELETE FROM call_decision_logs WHERE createdAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM call_decision_logs")
    suspend fun clear()
}

