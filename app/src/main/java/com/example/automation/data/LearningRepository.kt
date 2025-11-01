package com.example.automation.data

import android.content.Context
import com.example.automation.data.db.AppDatabase
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class LearningRepository private constructor(context: Context) {
    private val dao = AppDatabase.get(context).learningItemDao()

    fun observeItems(): Flow<List<LearningItem>> = dao.observeItems()

    fun observeDistinctTags(): Flow<List<String>> =
        observeItems().map { items ->
            items.flatMap { it.tags }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinctBy { it.lowercase() }
                .sortedBy { it.lowercase() }
        }

    fun observeSummary(): Flow<LearningSummary> = combine(
        dao.countAll(),
        dao.countByStatus(LearningStatus.DONE),
        dao.countByStatus(LearningStatus.IN_PROGRESS)
    ) { total, done, inProgress ->
        LearningSummary(total = total, done = done, inProgress = inProgress)
    }

    fun observeNextUp(): Flow<List<LearningItem>> = dao.observeNextUp(LearningStatus.DONE)

    suspend fun insert(item: LearningItem) {
        dao.insert(item)
    }

    suspend fun update(item: LearningItem) {
        dao.update(item)
    }

    suspend fun deleteItem(item: LearningItem) {
        dao.delete(item)
    }

    suspend fun findById(id: Long): LearningItem? = dao.findById(id)

    suspend fun updateStatus(id: Long, status: LearningStatus) {
        val current = dao.findById(id) ?: return
        val updated = current.copy(
            status = status,
            completedAt = if (status == LearningStatus.DONE) System.currentTimeMillis() else null
        )
        dao.update(updated)
    }

    suspend fun updateNote(id: Long, note: String) {
        val current = dao.findById(id) ?: return
        dao.update(current.copy(note = note))
    }

    suspend fun existsByUrl(url: String): Boolean = dao.existsByUrl(url)

    companion object {
        @Volatile private var INSTANCE: LearningRepository? = null

        fun getInstance(context: Context): LearningRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: LearningRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}

data class LearningSummary(
    val total: Int,
    val done: Int,
    val inProgress: Int
)
