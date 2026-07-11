package com.lsp.callguard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lsp.callguard.data.local.entity.DeviceContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceContactDao {

    @Query("SELECT * FROM device_contacts ORDER BY displayName COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<DeviceContactEntity>>

    @Query("SELECT COUNT(*) FROM device_contacts")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM device_contacts WHERE phoneE164 = :phoneE164")
    suspend fun countByPhone(phoneE164: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<DeviceContactEntity>)

    @Query("DELETE FROM device_contacts")
    suspend fun clear()
}

