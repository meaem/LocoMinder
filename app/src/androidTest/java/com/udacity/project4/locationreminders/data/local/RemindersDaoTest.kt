package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest

class RemindersDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase


    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderGetById() = runTest {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO(
            "title", "description", "location", 15.22, 17.2
        )
        val reminderDao = database.reminderDao()
        reminderDao.saveReminder(reminder)

        // WHEN - Get the reminder by id from the database.
        val loaded = reminderDao.getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded, notNullValue())
        assertThat(loaded?.id, `is`(reminder.id))
        assertThat(loaded?.title, `is`(reminder.title))
        assertThat(loaded?.description, `is`(reminder.description))
        assertThat(loaded?.latitude, `is`(reminder.latitude))
        assertThat(loaded?.longitude, `is`(reminder.longitude))

    }

    fun getReminderByWrongId_returnNull() = runTest {
        // WHEN - Get the reminder by wrong id from the database.
        val loaded = database.reminderDao().getReminderById("Not Found")

        // THEN - The loaded data is null.
        assertThat(loaded, `is`(nullValue()))
    }

    @Test
    fun insertReminderTwiceWithSameId_GetById_lastReminderSaved() = runTest {
        // GIVEN - Insert a reminder twice with different data except the id.
        val reminder = ReminderDTO(
            "title", "description", "location", 15.22, 17.2
        )
        val reminderDao = database.reminderDao()
        reminderDao.saveReminder(reminder)
        val reminder2 = reminder.copy(title = "updated title")
        reminderDao.saveReminder(reminder2)

        // WHEN - Get the reminder by id from the database.
        val loaded = reminderDao.getReminderById(reminder.id)

        // THEN - The loaded data contains the last saved reminder.
        assertThat<ReminderDTO>(loaded, notNullValue())
        assertThat(loaded?.id, `is`(reminder2.id))
        assertThat(loaded?.title, `is`(reminder2.title))
        assertThat(loaded?.description, `is`(reminder2.description))
        assertThat(loaded?.latitude, `is`(reminder2.latitude))
        assertThat(loaded?.longitude, `is`(reminder2.longitude))

    }

    @Test
    fun insertReminder_Delete_GetById_ReturnNull() = runTest {
        // GIVEN - Insert a reminder twice with different data except the id.
        val reminder = ReminderDTO(
            "title", "description", "location", 15.22, 17.2
        )
        val reminderDao = database.reminderDao()
        reminderDao.saveReminder(reminder)
        
        reminderDao.deleteReminder(reminder.id)
        // WHEN - Get the reminder by id from the database.
        val loaded = reminderDao.getReminderById(reminder.id)

        // THEN - The loaded data contains the last saved reminder.
        assertThat(loaded, nullValue())

    }

}