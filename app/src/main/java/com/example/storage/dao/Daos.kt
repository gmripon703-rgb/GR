package com.example.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.storage.entity.ChatMessageEntity
import com.example.storage.entity.DownloadedModelEntity
import com.example.storage.entity.RagChunkEntity
import com.example.storage.entity.RagDocumentEntity
import com.example.storage.entity.TeamRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface RagDao {
    @Query("SELECT * FROM rag_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<RagDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: RagDocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<RagChunkEntity>)

    @Query("DELETE FROM rag_documents WHERE id = :docId")
    suspend fun deleteDocument(docId: Long)

    @Query("DELETE FROM rag_chunks WHERE docId = :docId")
    suspend fun deleteChunksForDoc(docId: Long)

    @Query("SELECT * FROM rag_chunks")
    suspend fun getAllChunks(): List<RagChunkEntity>
}

@Dao
interface TeamRuleDao {
    @Query("SELECT * FROM team_rules ORDER BY priority ASC, id ASC")
    fun getAllRules(): Flow<List<TeamRuleEntity>>

    @Query("SELECT * FROM team_rules WHERE isEnabled = 1 ORDER BY priority ASC")
    suspend fun getActiveRules(): List<TeamRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: TeamRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rules: List<TeamRuleEntity>)

    @Update
    suspend fun updateRule(rule: TeamRuleEntity)

    @Query("DELETE FROM team_rules WHERE id = :id")
    suspend fun deleteRule(id: Long)

    @Query("SELECT COUNT(*) FROM team_rules")
    suspend fun getCount(): Int
}

@Dao
interface ModelDao {
    @Query("SELECT * FROM downloaded_models ORDER BY installedAt DESC")
    fun getAllModels(): Flow<List<DownloadedModelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: DownloadedModelEntity)

    @Query("DELETE FROM downloaded_models WHERE modelId = :modelId")
    suspend fun deleteModel(modelId: String)

    @Query("UPDATE downloaded_models SET isLoaded = CASE WHEN modelId = :activeId THEN 1 ELSE 0 END")
    suspend fun setActiveModel(activeId: String)
}
