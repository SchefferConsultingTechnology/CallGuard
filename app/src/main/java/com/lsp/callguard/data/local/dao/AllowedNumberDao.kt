package com.lsp.callguard.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lsp.callguard.data.local.entity.AllowedNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AllowedNumberDao {

    @Query("SELECT * FROM allowed_numbers ORDER BY label COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<AllowedNumberEntity>>

    @Query("SELECT COUNT(*) FROM allowed_numbers")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM allowed_numbers WHERE phoneE164 = :phoneE164")
    suspend fun countByPhone(phoneE164: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AllowedNumberEntity)

    @Delete
    suspend fun delete(entity: AllowedNumberEntity)

    @Query("DELETE FROM allowed_numbers WHERE id = :id")
    suspend fun deleteById(id: String)
}