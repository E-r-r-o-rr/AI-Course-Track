package com.example.automation

import android.content.Context
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.automation.data.db.AppDatabase
import com.example.automation.model.LearningCategory
import com.example.automation.model.LearningItem
import com.example.automation.model.LearningStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun clearDatabaseBeforeTest() = runBlocking {
        AppDatabase.get(context).clearAllTables()
    }

    @After
    fun clearDatabaseAfterTest() = runBlocking {
        AppDatabase.get(context).clearAllTables()
    }

    @Test
    fun dashboardDisplaysEmptyStates() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(isRoot()).perform(waitFor(500))

            onView(withId(R.id.totalCount)).check(matches(withText("0")))
            onView(withId(R.id.progressCount)).check(matches(withText("0")))
            onView(withId(R.id.doneCount)).check(matches(withText("0")))

            onView(withId(R.id.emptyCurrentTask)).check(matches(isDisplayed()))
            onView(withId(R.id.emptyQueued)).check(matches(isDisplayed()))

            onView(withId(R.id.dashboardScroll)).perform(ViewActions.swipeUp())
            onView(withId(R.id.emptyCompleted)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun learningListShowsInsertedItemAndNavigatesToDetail() = runBlocking {
        val dao = AppDatabase.get(context).learningItemDao()
        val seededItem = LearningItem(
            title = "Compose Basics",
            url = "https://example.com/compose",
            source = "Compose Academy",
            category = LearningCategory.COURSE,
            tags = listOf("Compose", "UI"),
            status = LearningStatus.IN_PROGRESS,
            queued = false,
            note = "",
            addedAt = 1000L,
            completedAt = null
        )
        dao.insert(seededItem)

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(isRoot()).perform(waitFor(500))

            onView(withId(R.id.learningListFragment)).perform(ViewActions.click())
            onView(isRoot()).perform(waitFor(500))

            onView(withId(R.id.recyclerView)).check(matches(hasDescendant(withText("Compose Basics"))))

            onView(withText("Compose Basics")).perform(ViewActions.click())
            onView(isRoot()).perform(waitFor(500))

            onView(withId(R.id.categoryLabel)).check(matches(withText(R.string.category_course)))
            onView(withId(R.id.source)).check(matches(withText(context.getString(R.string.detail_source_format, "Compose Academy"))))
            onView(withId(R.id.tagsGroup)).check(matches(hasDescendant(withText("Compose"))))
            onView(withId(R.id.buttonDoing)).check(matches(isChecked()))
        }
    }

    private fun waitFor(delayMillis: Long): ViewAction = object : ViewAction {
        override fun getConstraints() = isRoot()

        override fun getDescription() = "Wait for $delayMillis milliseconds"

        override fun perform(uiController: UiController, view: View?) {
            uiController.loopMainThreadForAtLeast(delayMillis)
        }
    }
}
