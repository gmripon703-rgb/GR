package com.example.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.storage.dao.ChatDao
import com.example.storage.dao.ModelDao
import com.example.storage.dao.RagDao
import com.example.storage.dao.TeamRuleDao
import com.example.storage.entity.ChatMessageEntity
import com.example.storage.entity.DownloadedModelEntity
import com.example.storage.entity.RagChunkEntity
import com.example.storage.entity.RagDocumentEntity
import com.example.storage.entity.TeamRuleEntity

@Database(
    entities = [
        ChatMessageEntity::class,
        RagDocumentEntity::class,
        RagChunkEntity::class,
        TeamRuleEntity::class,
        DownloadedModelEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun ragDao(): RagDao
    abstract fun teamRuleDao(): TeamRuleDao
    abstract fun modelDao(): ModelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "devsec_modular_ai.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
