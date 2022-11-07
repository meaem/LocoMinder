package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.FakeRemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import org.koin.test.junit5.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : AutoCloseKoinTest() {
    private lateinit var viewModel: SaveReminderViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setUp() {
        val testModule = module {
            viewModel {
                SaveReminderViewModel(
                    app = getApplicationContext(),
                    dataSource = get()
                )
            }

            single<ReminderDataSource> { FakeRemindersLocalRepository(mutableListOf()) }
        }

        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(testModule))
        }

        viewModel = get()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun titleEmpty_DisplaySnackBarError() {

        viewModel.validateEnteredData(ReminderDataItem("", "", "", 0.0, 0.0))
        val x = viewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(x, `is`(R.string.err_enter_title))
    }

    @Test
    fun titleNull_DisplaySnackBarError() {

        viewModel.validateEnteredData(ReminderDataItem(null, "", "", 0.0, 0.0))
        val x = viewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(x, `is`(R.string.err_enter_title))
    }


    @Test
    fun locationEmpty_DisplaySnackBarError() {

        viewModel.validateEnteredData(ReminderDataItem("Title", "", "", 0.0, 0.0))
        val x = viewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(x, `is`(R.string.err_select_location))
    }

    @Test
    fun locationNull_DisplaySnackBarError() {

        viewModel.validateEnteredData(ReminderDataItem("Title", "", null, 0.0, 0.0))
        val x = viewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(x, `is`(R.string.err_select_location))
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun locationNull_SaveReminder_DisplaySnackBarError() = runTest {

        viewModel.saveReminder(ReminderDataItem("Title", "", null, 0.0, 0.0))

        val x = viewModel.showSnackBarInt.getOrAwaitValue()
        val saved = viewModel.remiderSavedLocally.getOrAwaitValue()

        assertThat(x, `is`(R.string.err_select_location))
        assertThat(saved, `is`(false))

    }


//
}