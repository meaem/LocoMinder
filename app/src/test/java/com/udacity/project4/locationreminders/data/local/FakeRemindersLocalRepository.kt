package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeRemindersLocalRepository(private val reminders: MutableList<ReminderDTO>) :
    ReminderDataSource {

    private var shouldReturnError = false

    fun setReturnError(shouldReturnError: Boolean) {
        this.shouldReturnError = shouldReturnError
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (!shouldReturnError) {
            Result.Success(reminders)
        } else {
            Result.Error("No Reminders Found")
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (!shouldReturnError) {
            reminders.firstOrNull() { it.id == id }?.let {
                return Result.Success(it)
            }

            return Result.Error("Not found", -1)
        }

        return Result.Error("Error", -2)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    override suspend fun deleteReminder(reminderID: String) {
        reminders.removeIf {
            it.id == reminderID
        }
    }

}
