package com.udacity.project4.locationreminders.savereminder


import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.FakeRemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
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
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


private const val FAKE_STRING = "HELLO WORLD"


@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : AutoCloseKoinTest() {
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var repository: ReminderDataSource

    @Mock
    private lateinit var mockContext: Application

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setUp() {
        val testModule = module {
            viewModel {
                SaveReminderViewModel(
                    app = get(),
                    dataSource = get()
                )
            }

            single<ReminderDataSource> { FakeRemindersLocalRepository(mutableListOf()) }

        }

        startKoin {

            mockContext = mock {
                on { getString(R.string.reminder_saved) } doReturn "Reminder Saved !"
            }

            androidContext(mockContext)
            modules(listOf(testModule))
        }

        viewModel = get()
        repository = get()
    }

    @After
    fun tearDown() {
        stopKoin()

    }

    @Test
    fun titleEmpty_Validate_DisplaySnackBarError() {

        viewModel.validateEnteredData(ReminderDataItem("", "", "", 0.0, 0.0))
        val x = viewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(x, `is`(R.string.err_enter_title))
    }

    @Test
    fun titleNull_Validate_DisplaySnackBarError() {

        viewModel.validateEnteredData(ReminderDataItem(null, "", "", 0.0, 0.0))
        val x = viewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(x, `is`(R.string.err_enter_title))
    }


    @Test
    fun locationEmpty_Validate_DisplaySnackBarError() {

        viewModel.validateEnteredData(ReminderDataItem("Title", "", "", 0.0, 0.0))
        val x = viewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(x, `is`(R.string.err_select_location))
    }

    @Test
    fun locationNull_Validate_DisplaySnackBarError() {

        viewModel.validateEnteredData(ReminderDataItem("Title", "", null, 0.0, 0.0))
        val x = viewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(x, `is`(R.string.err_select_location))
    }


    @Test
    fun locationNull_SaveReminder_DisplaySnackBarError() {

        viewModel.validateAndSaveReminder(ReminderDataItem("Title", "", null, 0.0, 0.0))


        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
        assertThat(viewModel.remiderSavedLocally.getOrAwaitValue(), `is`(false))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun normalReminder_SaveReminder_DisplaySuccess() {
        mainCoroutineRule.pauseDispatcher()
        viewModel.validateAndSaveReminder(ReminderDataItem("Title", "", "Loc1", 0.0, 0.0))

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()


        assertThat(
            viewModel.showToast.getOrAwaitValue(),
            `is`(mockContext.getString(R.string.reminder_saved))
        )
        assertThat(viewModel.remiderSavedLocally.getOrAwaitValue(), `is`(true))

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))

    }


//
}