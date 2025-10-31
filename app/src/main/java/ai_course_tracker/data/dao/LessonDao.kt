package com.example.automation.data.dao

import androidx.room.*
import com.example.automation.model.Lesson
import com.example.automation.model.LessonStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY id DESC")
    fun getLessonsForCourse(courseId: Long): Flow<List<Lesson>>

    @Insert suspend fun insert(lesson: Lesson): Long
    @Update suspend fun update(lesson: Lesson)
    @Delete suspend fun delete(lesson: Lesson)

    @Query("UPDATE lessons SET status = :status WHERE id = :lessonId")
    suspend fun updateStatus(lessonId: Long, status: LessonStatus)

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId")
    suspend fun countLessons(courseId: Long): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId AND status = 'DONE'")
    suspend fun countDone(courseId: Long): Int

    @Query("""
    SELECT * FROM lessons 
    WHERE status != 'DONE'
    ORDER BY CASE WHEN dueAt = 0 THEN 1 ELSE 0 END, dueAt ASC, id DESC
    LIMIT 10
""")
    fun getNextUp(): kotlinx.coroutines.flow.Flow<List<Lesson>>

}
