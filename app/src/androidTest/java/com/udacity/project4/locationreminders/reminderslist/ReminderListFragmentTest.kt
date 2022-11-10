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
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersFakeLocalRepository
//import com.udacity.project4.locationreminders.data.local.FakeReminderDataSource
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@OptIn(ExperimentalCoroutinesApi::class)
@MediumTest
//@RunWith(AndroidJUnit4::class)
internal class ReminderListFragmentTest : AutoCloseKoinTest() {
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var datasource: ReminderDataSource


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



            single<ReminderDataSource> { RemindersFakeLocalRepository(mutableListOf()) }

        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(myModule))
        }

        viewModel = get()
        datasource = get()


    }

    @After
    fun tearDown() {
        stopKoin()
    }




    @Test
    fun remiderList_hasNoItems() = runTest {

        // GIVEN - On the home screen
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_Project4)

        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(0)))


    }

    @Test
    fun remiderList_hasOneItem() = runTest {

        datasource.saveReminder(ReminderDTO("Title", "Desc", "Location", 1.0, 2.0))

        // GIVEN - On the home screen
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_Project4)


//        viewModel.loadReminders()
        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(1)))
    }

    @Test
    fun remiderList_manyItems() = runTest {

        datasource.saveReminder(ReminderDTO("Title1", "Desc1", "Location1", 1.0, 2.0))
        datasource.saveReminder(ReminderDTO("Title2", "Desc2", "Location2", 1.0, 2.0))
        datasource.saveReminder(ReminderDTO("Title3", "Desc3", "Location3", 1.0, 2.0))
        datasource.saveReminder(ReminderDTO("Title4", "Desc4", "Location4", 1.0, 2.0))

        // GIVEN - On the home screen
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_Project4)


        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(4)))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(withText("Title1"))))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(withText("Title2"))))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(withText("Title3"))))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(withText("Title4"))))


    }

    @Test
    fun clickAddLocationButton_navigateToSelectLocationFragment() = runTest {

        // GIVEN - On the home screen
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_Project4)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the Save Reminder Fragment
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }
}