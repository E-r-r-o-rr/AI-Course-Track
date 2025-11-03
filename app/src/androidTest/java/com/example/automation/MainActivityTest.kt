package com.example.automation

import androidx.test.espresso.Espresso.onView
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun clearDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("learning_items.db")
    }

    @Test
    fun dashboardIsDisplayedByDefault() {
        onView(withId(R.id.subtitle))
            .check(matches(allOf(isDisplayed(), withText(R.string.dashboard_subtitle))))
        onView(withId(R.id.summaryCard))
            .check(matches(isDisplayed()))
    }

    @Test
    fun switchingToLibraryShowsLibraryUi() {
        onView(withId(R.id.learningListFragment)).perform(click())

        onView(withId(R.id.librarySubtitle))
            .check(matches(allOf(isDisplayed(), withText(R.string.library_subtitle))))
        onView(withId(R.id.createLearningItem))
            .check(matches(isDisplayed()))
    }

    @Test
    fun viewLibraryShortcutNavigatesFromDashboard() {
        onView(withId(R.id.viewLibraryButton)).perform(scrollTo(), click())

        onView(withId(R.id.librarySubtitle))
            .check(matches(allOf(isDisplayed(), withText(R.string.library_subtitle))))
    }

    @Test
    fun libraryShowsEmptyStateWhenNoItemsAreSaved() {
        onView(withId(R.id.learningListFragment)).perform(click())

        onView(withId(R.id.emptyState))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun tappingCreateButtonInLibraryOpensCreateScreen() {
        onView(withId(R.id.learningListFragment)).perform(click())

        onView(withId(R.id.createLearningItem)).perform(scrollTo(), click())

        onView(withText(R.string.create_title))
            .check(matches(isDisplayed()))
    }
}
