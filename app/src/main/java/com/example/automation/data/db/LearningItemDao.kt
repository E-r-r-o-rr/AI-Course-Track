package com.example.automation.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningItemDao {
    @Query("SELECT * FROM learning_items ORDER BY addedAt DESC")
    fun observeItems(): Flow<List<LearningItem>>

    @Insert
    suspend fun insert(item: LearningItem): Long

    @Update
    suspend fun update(item: LearningItem)

    @Query("DELETE FROM learning_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM learning_items WHERE id = :id")
    suspend fun findById(id: Long): LearningItem?

    @Query("SELECT COUNT(*) FROM learning_items WHERE status = :status")
    fun countByStatus(status: LearningStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_items")
    fun countAll(): Flow<Int>

    @Query("SELECT * FROM learning_items WHERE status = :status ORDER BY addedAt ASC")
    fun observeByStatus(status: LearningStatus): Flow<List<LearningItem>>

}
