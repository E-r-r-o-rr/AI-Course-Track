package com.example.automation.data.dao

import androidx.room.*
import com.example.automation.model.Course
import com.example.automation.model.Lesson
import kotlinx.coroutines.flow.Flow

data class CourseWithLessons(
    @Embedded val course: Course,
    @Relation(parentColumn = "id", entityColumn = "courseId")
    val lessons: List<Lesson>
)

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY id DESC")
    fun getCourses(): Flow<List<Course>>

    @Transaction
    @Query("SELECT * FROM courses ORDER BY id DESC")
    fun getCoursesWithLessons(): Flow<List<CourseWithLessons>>

    @Insert suspend fun insert(course: Course): Long
    @Update suspend fun update(course: Course)
    @Delete suspend fun delete(course: Course)

    @Query("DELETE FROM courses") suspend fun clear()
}
