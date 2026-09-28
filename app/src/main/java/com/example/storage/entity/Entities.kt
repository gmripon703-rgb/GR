package com.example.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user", "model"
    val content: String,
    val modelUsed: String = "on-device",
    val providerType: String = "LOCAL", // "LOCAL", "CLOUD", "CUSTOM"
    val timestamp: Long = System.currentTimeMillis(),
    val isCodeAudit: Boolean = false,
    val latencyMs: Long = 0
)

@Entity(tableName = "rag_documents")
data class RagDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val fileType: String, // "MARKDOWN", "TEXT", "KOTLIN", "PYTHON", "DOC"
    val rawContent: String,
    val chunkCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "rag_chunks")
data class RagChunkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val docId: Long,
    val chunkIndex: Int,
    val content: String,
    val keywords: String = ""
)

@Entity(tableName = "team_rules")
data class TeamRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "SECURITY", "CODING_STANDARDS", "DEPENDENCIES", "PRIVACY", "ARCHITECTURE"
    val priority: Int = 2, // 1 = Security (Highest), 2 = Team Rules, 3 = Project Rules, 4 = User Prefs
    val ruleContent: String,
    val isEnabled: Boolean = true,
    val isStrict: Boolean = true
)

@Entity(tableName = "downloaded_models")
data class DownloadedModelEntity(
    @PrimaryKey
    val modelId: String,
    val name: String,
    val format: String,
    val localFilePath: String,
    val sizeBytes: Long,
    val sha256: String,
    val version: String,
    val isLoaded: Boolean = false,
    val installedAt: Long = System.currentTimeMillis()
)
