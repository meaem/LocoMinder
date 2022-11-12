package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.local.FakeRemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemindersListViewModelTest {
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var repository: FakeRemindersLocalRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Test
    fun loadReminders_callErrorToDisplay() {
        repository = FakeRemindersLocalRepository(mutableListOf())
        viewModel = RemindersListViewModel(getApplicationContext(), repository)

        // Make the repository return errors.
        repository.setReturnError(true)
        viewModel.loadReminders()

        // Then showSnackBar value is changed (which triggers a snackbar to be shown).
        assertThat(
            viewModel.showSnackBar.getOrAwaitValue(),
            `is`(FakeRemindersLocalRepository.TEST_EXCEPTION)
        )
    }

}