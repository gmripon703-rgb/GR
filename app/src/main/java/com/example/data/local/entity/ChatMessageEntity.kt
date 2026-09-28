package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user" or "model"
    val content: String,
    val modelUsed: String = "gemini-3.5-flash",
    val timestamp: Long = System.currentTimeMillis(),
    val isCodeAudit: Boolean = false
)
