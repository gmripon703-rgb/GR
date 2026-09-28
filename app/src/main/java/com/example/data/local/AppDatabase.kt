package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AuditDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.OwaspDao
import com.example.data.local.entity.AuditFindingEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.OwaspItemEntity

@Database(
    entities = [
        ChatMessageEntity::class,
        AuditFindingEntity::class,
        OwaspItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun auditDao(): AuditDao
    abstract fun owaspDao(): OwaspDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "devsec_ai_local.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
