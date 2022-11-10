package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    val selectedPOI = MutableLiveData<PointOfInterest?>()
//    val latitude = MutableLiveData<Double?>()
//    val longitude = MutableLiveData<Double?>()

    val remiderSavedLocally: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val mapReady: SingleLiveEvent<Boolean> = SingleLiveEvent()


    init {
        mapReady.postValue(false);
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */


    fun onClear() {
        Log.d("ViewModel", "onClear")
        reminderTitle.postValue(null)
        reminderDescription.postValue(null)
        reminderSelectedLocationStr.postValue(null)
        selectedPOI.postValue(null)
        remiderSavedLocally.postValue(false)
        mapReady.postValue(false)

    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
        }
    }

    /**
     * Save the reminder to the data source
     */
    private fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )

            )
            remiderSavedLocally.value = true

            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    fun deleteRemider(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.deleteReminder(reminderData.id)
            showLoading.value = false
            showSnackBarInt.value = R.string.reminder_deleted
//            showToast.value = app.getString(R.string.reminder_deleted)
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            remiderSavedLocally.postValue(false)
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            remiderSavedLocally.postValue(false)

            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }


}