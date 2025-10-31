package com.example.automation.model

import androidx.room.*
import java.util.*

enum class LessonStatus { TODO, DOING, DONE }

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val provider: String,
    // tags stored via converter
    val tags: List<String> = emptyList()
)

@Entity(
    tableName = "lessons",
    foreignKeys = [ForeignKey(
        entity = Course::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("courseId")]
)
data class Lesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val title: String,
    val status: LessonStatus = LessonStatus.TODO,
    // store due date as epoch millis (0 = unset)
    val dueAt: Long = 0L,
    val tags: List<String> = emptyList()
)

@Entity(
    tableName = "sessions",
    foreignKeys = [ForeignKey(
        entity = Lesson::class,
        parentColumns = ["id"],
        childColumns = ["lessonId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("lessonId")]
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lessonId: Long,
    val startedAtMs: Long,
    val durationSec: Long,
    val notes: String
)
