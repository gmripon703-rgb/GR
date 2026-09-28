package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AuditFindingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_findings ORDER BY timestamp DESC")
    fun getAllFindings(): Flow<List<AuditFindingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFindings(findings: List<AuditFindingEntity>)

    @Query("DELETE FROM audit_findings")
    suspend fun clearFindings()
}
