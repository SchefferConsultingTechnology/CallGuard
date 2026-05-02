package com.lsp.callguard.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lsp.callguard.data.local.dao.AllowedNumberDao
import com.lsp.callguard.data.local.entity.AllowedNumberEntity

@Database(
    entities = [
        AllowedNumberEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CallGuardDatabase : RoomDatabase() {

    abstract fun allowedNumberDao(): AllowedNumberDao

    companion object {
        @Volatile
        private var INSTANCE: CallGuardDatabase? = null

        fun getInstance(context: Context): CallGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CallGuardDatabase::class.java,
                    "callguard.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

