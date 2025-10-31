package com.example.automation.data

import android.content.Context
import com.example.automation.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Repository private constructor(ctx: Context) {
    private val db = AppDatabase.get(ctx)
    private val courseDao = db.courseDao()
    private val lessonDao = db.lessonDao()
    private val sessionDao = db.sessionDao()

    // Courses with progress % (derived from lessons DONE)
    fun getCoursesUi(): Flow<List<CourseUi>> =
        courseDao.getCoursesWithLessons().map { list ->
            list.map { cw ->
                val done = cw.lessons.count { it.status == LessonStatus.DONE }
                val percent = if (cw.lessons.isEmpty()) 0 else (done * 100 / cw.lessons.size)
                CourseUi(
                    id = cw.course.id,
                    title = cw.course.title,
                    provider = cw.course.provider,
                    lessons = cw.lessons.size,
                    progressPercent = percent,
                    tags = cw.course.tags
                )
            }
        }

    fun getLessons(courseId: Long): Flow<List<Lesson>> = lessonDao.getLessonsForCourse(courseId)
    suspend fun addLesson(courseId: Long, title: String) = lessonDao.insert(Lesson(courseId = courseId, title = title))
    suspend fun setLessonStatus(id: Long, status: LessonStatus) = lessonDao.updateStatus(id, status)
    suspend fun deleteLesson(lesson: Lesson) = lessonDao.delete(lesson)

    fun getSessions(lessonId: Long) = sessionDao.getSessionsForLesson(lessonId)
    suspend fun saveSession(lessonId: Long, startedAt: Long, durationSec: Long, notes: String) =
        sessionDao.insert(Session(lessonId = lessonId, startedAtMs = startedAt, durationSec = durationSec, notes = notes))

    fun totalDurationSec(): Flow<Long> = sessionDao.getTotalDurationSec().map { it ?: 0L }
    fun sessionsThisWeek(): Flow<Int> {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000
        return sessionDao.countSince(sevenDaysAgo)
    }

    companion object {
        @Volatile private var INSTANCE: Repository? = null
        fun get(ctx: Context): Repository = INSTANCE ?: synchronized(this) { INSTANCE ?: Repository(ctx.applicationContext).also { INSTANCE = it } }
    }
}

// UI model matching your adapter
data class CourseUi(
    val id: Long,
    val title: String,
    val provider: String,
    val lessons: Int,
    val progressPercent: Int,
    val tags: List<String>
)
