package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owasp_items")
data class OwaspItemEntity(
    @PrimaryKey
    val code: String, // e.g. "A01", "M01"
    val category: String, // "WEB" or "MOBILE"
    val title: String,
    val description: String,
    val defenseStrategy: String,
    val status: String = "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "PASSED", "FLAGGED"
    val notes: String = ""
)
