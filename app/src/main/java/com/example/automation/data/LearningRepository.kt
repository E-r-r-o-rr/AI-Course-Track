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

    fun observeCurrentTasks(): Flow<List<LearningItem>> =
        dao.observeByStatus(LearningStatus.IN_PROGRESS)

    fun observeQueuedItems(): Flow<List<LearningItem>> =
        observeItems().map { items ->
            items.filter { it.queued && it.status == LearningStatus.TODO }
        }

    fun observeCompletedItems(): Flow<List<LearningItem>> =
        dao.observeByStatus(LearningStatus.DONE).map { items ->
            items.sortedByDescending { it.completedAt ?: it.addedAt }
        }

    suspend fun addToQueue(id: Long) {
        val current = dao.findById(id) ?: return
        dao.update(
            current.copy(
                status = LearningStatus.TODO,
                queued = true,
                completedAt = null
            )
        )
    }

    suspend fun removeFromQueue(id: Long) {
        val current = dao.findById(id) ?: return
        dao.update(current.copy(queued = false))
    }

    suspend fun startItem(id: Long) {
        val current = dao.findById(id) ?: return
        dao.update(
            current.copy(
                status = LearningStatus.IN_PROGRESS,
                queued = false,
                completedAt = null
            )
        )
    }

    suspend fun moveToQueue(id: Long) {
        val current = dao.findById(id) ?: return
        dao.update(
            current.copy(
                status = LearningStatus.TODO,
                queued = true,
                completedAt = null
            )
        )
    }

    suspend fun completeItem(id: Long) {
        val current = dao.findById(id) ?: return
        dao.update(
            current.copy(
                status = LearningStatus.DONE,
                queued = false,
                completedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun insert(item: LearningItem) {
        dao.insert(item)
    }

    suspend fun update(item: LearningItem) {
        dao.update(item)
    }

    suspend fun deleteItem(item: LearningItem) {
        dao.deleteById(item.id)
    }

    suspend fun deleteItem(id: Long) {
        dao.deleteById(id)
    }

    suspend fun findById(id: Long): LearningItem? = dao.findById(id)

    suspend fun updateStatus(id: Long, status: LearningStatus) {
        val current = dao.findById(id) ?: return
        val updated = current.copy(
            status = status,
            queued = if (status == LearningStatus.DONE) false else current.queued,
            completedAt = if (status == LearningStatus.DONE) System.currentTimeMillis() else null
        )
        dao.update(updated)
    }

    suspend fun updateNote(id: Long, note: String) {
        val current = dao.findById(id) ?: return
        dao.update(current.copy(note = note))
    }

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
