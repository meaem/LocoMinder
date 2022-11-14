package com.udacity.project4.utils

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

private val TAG = "LocationServicesUtils"
const val REQUEST_TURN_DEVICE_LOCATION_ON = 29


//fun showEnableLocationSetting(activity: Activity, whatToDoWhenEnabled: () -> Unit) {
//    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
//        .setMinUpdateIntervalMillis(500)
//        .setMaxUpdateDelayMillis(1000)
//        .setMaxUpdates(5)
//
//    val builder = LocationSettingsRequest.Builder()
//        .addLocationRequest(request.build())
//
//    val task = LocationServices.getSettingsClient(activity)
//        .checkLocationSettings(builder.build())
//
//    task.addOnCompleteListener {
//        Log.d(TAG, "LocationServices Complete!!")
//    }
//    task.addOnSuccessListener { response ->
//        Log.d(TAG, "LocationServices Success!!")
//        val states = response.locationSettingsStates
//        if (states != null) {
//            if (states.isLocationPresent) {
//                Log.d(TAG, "Done!!")
//                whatToDoWhenEnabled()
//            } else {
//                Log.d(TAG, "else 1  !!")
//            }
//        } else {
//            Log.d(TAG, "else 2 !!")
//        }
//    }
//    task.addOnFailureListener { e ->
//        Log.d(TAG, "LocationServices Failure!!")
//        if (e is ResolvableApiException) {
//            try {
//                // Handle result in onActivityResult()
//                e.startResolutionForResult(
//                    activity,
//                    REQUEST_TURN_DEVICE_LOCATION_ON
//                )
//            } catch (sendEx: IntentSender.SendIntentException) {
//
//                sendEx.printStackTrace()
//            }
//        } else {
//
//            Log.d(TAG, "else 3!!")
//            e.printStackTrace()
//        }
//    }
//}

fun Fragment.checkDeviceLocationSettings(
    activity: Activity,
    whatToDoWhenEnabled: () -> Unit,
    whatToDoWhenNotEnabled: () -> Unit,
    resolve: Boolean = true
) {

    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
        .setMinUpdateIntervalMillis(500)
        .setMaxUpdateDelayMillis(1000)
        .setMaxUpdates(5)

//
    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(request.build())


    val settingsClient = LocationServices.getSettingsClient(activity)

    val task = settingsClient.checkLocationSettings(builder.build())

    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException && resolve) {
            try {
//                exception.startResolutionForResult(
//                    activity,
//                    REQUEST_TURN_DEVICE_LOCATION_ON
//                )

                startIntentSenderForResult(
                    exception.getResolution().getIntentSender(),
                    REQUEST_TURN_DEVICE_LOCATION_ON,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (sendEx: IntentSender.SendIntentException) {
                Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
            }
        } else {
            Log.d(TAG, exception.toString())
            whatToDoWhenNotEnabled()
        }
    }


    task.addOnCompleteListener {

        if (it.isSuccessful) {
            whatToDoWhenEnabled()
        }
    }


}