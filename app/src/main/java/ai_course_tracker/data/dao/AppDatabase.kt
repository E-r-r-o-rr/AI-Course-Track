package com.example.automation.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.automation.data.dao.CourseDao
import com.example.automation.data.dao.LessonDao
import com.example.automation.data.dao.SessionDao
import com.example.automation.model.Course
import com.example.automation.model.Lesson
import com.example.automation.model.LessonStatus
import com.example.automation.model.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Course::class, Lesson::class, Session::class],
    version = 1, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(context, AppDatabase::class.java, "automation.db")
                    .fallbackToDestructiveMigration()
                    .addCallback(SeedCallback(context))
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private class SeedCallback(private val ctx: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val database = get(ctx)
            CoroutineScope(Dispatchers.IO).launch {
                // Seed sample data (matches your current UI)
                val c1 = database.courseDao().insert(Course(title = "LLM Agent Systems", provider = "DeepLearning.AI", tags = listOf("Agents","LangChain","Tools")))
                val c2 = database.courseDao().insert(Course(title = "Make (Integromat) Basics", provider = "Make.com", tags = listOf("No-Code","Webhooks")))
                val c3 = database.courseDao().insert(Course(title = "Zapier Expert Track", provider = "Zapier", tags = listOf("Zaps","Formatter","APIs")))

                database.lessonDao().apply {
                    insert(Lesson(courseId = c1, title = "Prompt Engineering Basics"))
                    insert(Lesson(courseId = c1, title = "Tool Use & Actions", status = LessonStatus.DOING))
                    insert(Lesson(courseId = c1, title = "Evaluating Outputs", status = LessonStatus.DONE))
                }
            }
        }
    }
}
