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

    private lateinit var _reminderData: ReminderDataItem

    val reminderData: ReminderDataItem
        get() = _reminderData

    init {
        mapReady.postValue(false)
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
        showLoading.postValue(false)
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder() {
        if (validateEnteredData()) {
            saveReminder()
        }
    }

    /**
     * Save the reminder to the data source
     */
    private fun saveReminder() {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    _reminderData.title,
                    _reminderData.description,
                    _reminderData.location,
                    _reminderData.latitude,
                    _reminderData.longitude,
                    _reminderData.id
                )
            )
            remiderSavedLocally.postValue(true)
            showLoading.postValue(false)
            showToast.postValue(app.getString(R.string.reminder_saved))
            navigationCommand.postValue(NavigationCommand.Back)
        }
    }

    fun deleteRemider() {
        showLoading.value = true
        viewModelScope.launch {

            dataSource.deleteReminder(_reminderData.id)
            showLoading.value = false
            showSnackBarInt.value = R.string.reminder_deleted
//            showToast.value = app.getString(R.string.reminder_deleted)
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(): Boolean {

        _reminderData = ReminderDataItem(
            reminderTitle.value,
            reminderDescription.value,
            reminderSelectedLocationStr.value,
            selectedPOI.value?.latLng?.latitude,
            selectedPOI.value?.latLng?.longitude
        )

        if (_reminderData.title.isNullOrEmpty()) {
            remiderSavedLocally.postValue(false)
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (_reminderData.location.isNullOrEmpty()) {
            remiderSavedLocally.postValue(false)

            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

//    @VisibleForTesting
//    fun setDataItemForTest(item: ReminderDataItem) {
//        _reminderData = item
//    }
//
//    @VisibleForTesting
//    fun dataIsInitialized() = ::_reminderData.isInitialized
}