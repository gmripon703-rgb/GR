package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.OwaspItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OwaspDao {
    @Query("SELECT * FROM owasp_items ORDER BY code ASC")
    fun getAllItems(): Flow<List<OwaspItemEntity>>

    @Query("SELECT * FROM owasp_items WHERE category = :category ORDER BY code ASC")
    fun getItemsByCategory(category: String): Flow<List<OwaspItemEntity>>

    @Query("SELECT COUNT(*) FROM owasp_items")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<OwaspItemEntity>)

    @Update
    suspend fun updateItem(item: OwaspItemEntity)

    @Query("UPDATE owasp_items SET status = :status, notes = :notes WHERE code = :code")
    suspend fun updateStatus(code: String, status: String, notes: String)

    @Query("UPDATE owasp_items SET status = 'NOT_STARTED', notes = ''")
    suspend fun resetAllStatus()
}
