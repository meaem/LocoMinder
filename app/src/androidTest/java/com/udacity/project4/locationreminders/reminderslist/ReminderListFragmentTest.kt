package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.FakeRemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import org.koin.test.junit5.AutoCloseKoinTest
import org.mockito.Mockito

@MediumTest
//@RunWith(AndroidJUnit4::class)
internal class ReminderListFragmentTest : AutoCloseKoinTest() {
    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun init() {
        stopKoin()
//        TestingUtils.initKoin()

        val myModule = module {

            viewModel {
                RemindersListViewModel(
                    app = get(),
                    dataSource = get() as ReminderDataSource
                )
            }



            single<ReminderDataSource> { FakeRemindersLocalRepository(mutableListOf()) }

        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(myModule))
        }

        viewModel = get()


    }

    @After
    fun tearDown() {
//        TestingUtils.releaseKoin()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun remiderList_hasNoItems() = runTest {

        // GIVEN - On the home screen
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_Project4)

//val viewModel
        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(0)))


    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun remiderList_hasOneItems() = runTest {

        // GIVEN - On the home screen
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_Project4)


        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(0)))
        viewModel.loadReminders()


    }

    @Test
    fun clickAddLocationButton_navigateToSelectLocationFragment() = runTest {

        // GIVEN - On the home screen
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_Project4)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
//        // WHEN - Click on the first list item
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the first detail screen
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )


    }


}