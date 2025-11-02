package com.example.automation.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.automation.model.LearningCategory
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LearningItemDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: LearningItemDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
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
    fun insertAndFindById() = runBlocking {
        val id = dao.insert(sampleItem(title = "Async Kotlin", addedAt = 1_000L, tags = listOf("kotlin", "async")))

        val stored = dao.findById(id)

        assertNotNull(stored)
        assertEquals("Async Kotlin", stored?.title)
        assertEquals(LearningStatus.TODO, stored?.status)
        assertEquals(listOf("kotlin", "async"), stored?.tags)
    }

    @Test
    fun updateModifiesExistingItem() = runBlocking {
        val id = dao.insert(sampleItem(title = "Design Systems", addedAt = 2_000L))
        val initial = dao.findById(id)!!

        val updatedItem = initial.copy(
            status = LearningStatus.DONE,
            queued = false,
            note = "Wrapped up",
            completedAt = 5_000L
        )
        dao.update(updatedItem)

        val stored = dao.findById(id)
        assertNotNull(stored)
        assertEquals(LearningStatus.DONE, stored?.status)
        assertEquals("Wrapped up", stored?.note)
        assertEquals(5_000L, stored?.completedAt)
    }

    @Test
    fun deleteByIdRemovesItem() = runBlocking {
        val id = dao.insert(sampleItem(title = "Compose Animations", addedAt = 3_000L))

        dao.deleteById(id)

        val stored = dao.findById(id)
        assertNull(stored)
    }

    @Test
    fun observeQueriesReflectDatabaseState() = runBlocking {
        dao.insert(sampleItem(title = "Coroutines", status = LearningStatus.TODO, addedAt = 1_000L))
        dao.insert(sampleItem(title = "Compose Layouts", status = LearningStatus.IN_PROGRESS, addedAt = 3_000L))
        dao.insert(
            sampleItem(
                title = "Testing on Android",
                status = LearningStatus.DONE,
                addedAt = 2_000L,
                completedAt = 4_000L
            )
        )
        dao.insert(
            sampleItem(
                title = "Accessibility",
                status = LearningStatus.DONE,
                addedAt = 5_000L,
                completedAt = 6_000L
            )
        )

        val allItems = dao.observeItems().first()
        assertEquals(listOf("Accessibility", "Compose Layouts", "Testing on Android", "Coroutines"), allItems.map { it.title })

        val doneItems = dao.observeByStatus(LearningStatus.DONE).first()
        assertEquals(listOf("Testing on Android", "Accessibility"), doneItems.map { it.title })

        val totalCount = dao.countAll().first()
        val doneCount = dao.countByStatus(LearningStatus.DONE).first()
        val inProgressCount = dao.countByStatus(LearningStatus.IN_PROGRESS).first()

        assertEquals(4, totalCount)
        assertEquals(2, doneCount)
        assertEquals(1, inProgressCount)
    }

    private fun sampleItem(
        title: String,
        status: LearningStatus = LearningStatus.TODO,
        addedAt: Long,
        tags: List<String> = listOf("tag"),
        completedAt: Long? = if (status == LearningStatus.DONE) addedAt else null
    ): LearningItem = LearningItem(
        title = title,
        url = "https://example.com/${title.replace(' ', '-').lowercase()}",
        source = "Source for $title",
        category = LearningCategory.COURSE,
        tags = tags,
        status = status,
        queued = false,
        note = "",
        addedAt = addedAt,
        completedAt = completedAt
    )
}
