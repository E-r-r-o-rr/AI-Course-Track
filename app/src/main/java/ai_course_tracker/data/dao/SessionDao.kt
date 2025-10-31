package com.example.automation.data.dao

import androidx.room.*
import com.example.automation.model.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert suspend fun insert(session: Session): Long
    @Query("SELECT * FROM sessions WHERE lessonId = :lessonId ORDER BY startedAtMs DESC")
    fun getSessionsForLesson(lessonId: Long): Flow<List<Session>>

    @Query("SELECT SUM(durationSec) FROM sessions")
    fun getTotalDurationSec(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM sessions WHERE startedAtMs >= :fromMs")
    fun countSince(fromMs: Long): Flow<Int>
}
