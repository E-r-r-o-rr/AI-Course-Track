package com.example.automation

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
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
        onView(allOf(withId(R.id.learningListFragment), isDisplayed())).perform(click())

        onView(withId(R.id.librarySubtitle))
            .check(matches(allOf(isDisplayed(), withText(R.string.library_subtitle))))
        onView(withId(R.id.createLearningItem))
            .check(matches(isDisplayed()))
    }

    @Test
    fun viewLibraryShortcutNavigatesFromDashboard() {
        onView(withId(R.id.dashboardScroll)).perform(swipeUp())
        onView(allOf(withId(R.id.viewLibraryButton), isDisplayed())).perform(click())

        onView(withId(R.id.librarySubtitle))
            .check(matches(allOf(isDisplayed(), withText(R.string.library_subtitle))))
    }

    @Test
    fun libraryShowsEmptyStateWhenNoItemsAreSaved() {
        onView(allOf(withId(R.id.learningListFragment), isDisplayed())).perform(click())

        onView(withId(R.id.emptyState))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun tappingCreateButtonInLibraryOpensCreateScreen() {
        onView(allOf(withId(R.id.learningListFragment), isDisplayed())).perform(click())

        onView(withId(R.id.libraryScroll)).perform(swipeUp())
        onView(allOf(withId(R.id.createLearningItem), isDisplayed())).perform(click())

        onView(withText(R.string.create_title))
            .check(matches(isDisplayed()))
    }

    @Test
    fun dashboardEmptySectionsAreVisibleWhenNoData() {
        onView(withId(R.id.dashboardScroll)).perform(swipeUp(), swipeUp())

        onView(withId(R.id.emptyCurrentTask))
            .check(matches(isDisplayed()))
        onView(withId(R.id.emptyQueued))
            .check(matches(isDisplayed()))
        onView(withId(R.id.emptyCompleted))
            .check(matches(isDisplayed()))
    }

    @Test
    fun dashboardCreateButtonOpensCreateScreen() {
        onView(withId(R.id.dashboardScroll)).perform(swipeUp())
        onView(allOf(withId(R.id.createItemButton), isDisplayed())).perform(click())

        onView(withText(R.string.create_title))
            .check(matches(isDisplayed()))
        pressBack()

        onView(withId(R.id.subtitle))
            .check(matches(isDisplayed()))
    }

    @Test
    fun libraryEmptyStateCreateShortcutOpensCreateScreen() {
        onView(allOf(withId(R.id.learningListFragment), isDisplayed())).perform(click())

        onView(withId(R.id.libraryScroll)).perform(swipeUp())
        onView(allOf(withId(R.id.emptyCreateButton), isDisplayed())).perform(click())

        onView(withText(R.string.create_title))
            .check(matches(isDisplayed()))
    }

    @Test
    fun libraryFilterDefaultsToAllStatus() {
        onView(allOf(withId(R.id.learningListFragment), isDisplayed())).perform(click())

        onView(withId(R.id.chipAll))
            .check(matches(isChecked()))
        onView(withId(R.id.chipTodo))
            .check(matches(not(isChecked())))
        onView(withId(R.id.chipDoing))
            .check(matches(not(isChecked())))
        onView(withId(R.id.chipDone))
            .check(matches(not(isChecked())))
    }
}
