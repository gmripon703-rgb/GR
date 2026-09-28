package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_findings")
data class AuditFindingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val severity: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"
    val cweId: String,
    val matchedSnippet: String,
    val lineHint: Int,
    val explanation: String,
    val recommendation: String,
    val timestamp: Long = System.currentTimeMillis()
)
