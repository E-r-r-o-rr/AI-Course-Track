package com.example.automation.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.automation.model.LearningCategory
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearningItemDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: LearningItemDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.learningItemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndFindByIdReturnsItem() = runBlocking {
        val insertedId = dao.insert(sampleItem(title = "Learn Compose Basics"))

        val stored = dao.findById(insertedId)

        assertThat(stored, notNullValue())
        assertThat(stored?.id, `is`(insertedId))
        assertThat(stored?.title, `is`("Learn Compose Basics"))
    }

    @Test
    fun updateChangesPersistedState() = runBlocking {
        val insertedId = dao.insert(sampleItem(status = LearningStatus.TODO))
        val stored = requireNotNull(dao.findById(insertedId))

        val updated = stored.copy(status = LearningStatus.IN_PROGRESS, queued = true)
        dao.update(updated)

        val reloaded = dao.findById(insertedId)
        assertThat(reloaded?.status, `is`(LearningStatus.IN_PROGRESS))
        assertThat(reloaded?.queued, `is`(true))
    }

    @Test
    fun deleteRemovesRow() = runBlocking {
        val insertedId = dao.insert(sampleItem())
        val stored = requireNotNull(dao.findById(insertedId))

        dao.delete(stored)

        val deleted = dao.findById(insertedId)
        assertThat(deleted, `is`(nullValue()))
    }

    @Test
    fun observeByStatusEmitsFilteredResults() = runBlocking {
        dao.insert(sampleItem(title = "Compose", status = LearningStatus.TODO))
        dao.insert(sampleItem(title = "Room", status = LearningStatus.DONE))
        dao.insert(sampleItem(title = "Coroutines", status = LearningStatus.TODO))

        val todoItems = dao.observeByStatus(LearningStatus.TODO).first()

        assertThat(todoItems.size, `is`(2))
        assertThat(todoItems.all { it.status == LearningStatus.TODO }, `is`(true))
    }

    private fun sampleItem(
        title: String = "Read Android docs",
        status: LearningStatus = LearningStatus.IN_PROGRESS
    ): LearningItem {
        val now = System.currentTimeMillis()
        return LearningItem(
            id = 0,
            title = title,
            url = "https://developer.android.com",
            source = "Android",
            category = LearningCategory.COURSE,
            tags = listOf("android", "ui"),
            status = status,
            queued = false,
            note = "",
            addedAt = now,
            completedAt = null
        )
    }
}
