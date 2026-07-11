package com.lsp.callguard.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lsp.callguard.data.local.dao.AllowedNumberDao
import com.lsp.callguard.data.local.dao.CallDecisionLogDao
import com.lsp.callguard.data.local.dao.DeviceContactDao
import com.lsp.callguard.data.local.entity.AllowedNumberEntity
import com.lsp.callguard.data.local.entity.CallDecisionLogEntity
import com.lsp.callguard.data.local.entity.DeviceContactEntity

@Database(
    entities = [
        AllowedNumberEntity::class,
        CallDecisionLogEntity::class,
        DeviceContactEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class CallGuardDatabase : RoomDatabase() {

    abstract fun allowedNumberDao(): AllowedNumberDao

    abstract fun callDecisionLogDao(): CallDecisionLogDao

    abstract fun deviceContactDao(): DeviceContactDao

    companion object {
        @Volatile
        private var INSTANCE: CallGuardDatabase? = null

        fun getInstance(context: Context): CallGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CallGuardDatabase::class.java,
                    "callguard.db"
                ).fallbackToDestructiveMigration(dropAllTables=true)
                 .build().also { INSTANCE = it }
            }
        }
    }
}

