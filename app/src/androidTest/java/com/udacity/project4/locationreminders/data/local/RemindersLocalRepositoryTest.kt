package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result.Error
import com.udacity.project4.locationreminders.data.dto.Result.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

//    private lateinit var tasksLocalDataSource: RemindersFakeLocalRepository

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase


    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        localDataSource = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)


    }

    @After
    fun tearDown() = database.close()


    @Test
    fun saveReminder_retrievesReminder() = runTest {
        // GIVEN - A new reminder saved in the database.
        val newReminder = ReminderDTO("title", "description", "location", 0.0, 0.0)
        localDataSource.saveReminder(newReminder)

        // WHEN  - reminder retrieved by ID.
        val result = localDataSource.getReminder(newReminder.id)

        // THEN - Same reminder is returned.
        assertThat(result, `is`(Success(newReminder)))
        result as Success
        assertThat(result.data.title, `is`(newReminder.title))
        assertThat(result.data.description, `is`(newReminder.description))
        assertThat(result.data.location, `is`(newReminder.location))
        assertThat(result.data.longitude, `is`(newReminder.longitude))
        assertThat(result.data.latitude, `is`(newReminder.latitude))
    }

    @Test
    fun saveReminderThenDelete_retrievesReminder_returnsNull() = runTest {
        // GIVEN - A new reminder saved in the database then deleted.
        val newReminder = ReminderDTO("title", "description", "location", 0.0, 0.0)
        localDataSource.saveReminder(newReminder)
        localDataSource.deleteReminder(newReminder.id)

        // WHEN  - reminder retrieved by ID.
        val result = localDataSource.getReminder(newReminder.id)

        // THEN - return null .
        assertThat(result, `is`(Error("Reminder not found!")))
        result as Error
        assertThat(result.statusCode, `is`(nullValue()))
        assertThat(result.message, `is`("Reminder not found!"))

    }


    @Test
    fun addManyThendeleteAll_retrieveAll_returnsEmptyList() = runTest {
        // GIVEN - Many reminders saved in the database then deleted all reminders.
        val newReminder = ReminderDTO("title", "description", "location", 0.0, 0.0)
        localDataSource.saveReminder(newReminder)
        localDataSource.saveReminder(
            newReminder.copy(
                title = "t2",
                id = UUID.randomUUID().toString()
            )
        )
        localDataSource.saveReminder(
            newReminder.copy(
                title = "t3",
                id = UUID.randomUUID().toString()
            )
        )

        localDataSource.deleteAllReminders()

        // WHEN  - get all reminders.
        val result = localDataSource.getReminders()

        // THEN - return empty list of reminders .
        assertThat(result, `is`(Success(emptyList())))
        result as Success
        assertThat(result.data.size, `is`(0))
    }

}