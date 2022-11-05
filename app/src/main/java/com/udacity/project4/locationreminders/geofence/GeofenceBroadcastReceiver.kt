package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent


/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val TAG = GeofenceBroadcastReceiver::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive1 intent Type= ${intent?.javaClass?.simpleName}")
        if (context != null) {
            if (intent != null) {
                Log.d(TAG, "onReceive2: intent = $intent")
                val event = GeofencingEvent.fromIntent(intent)
                Log.d(TAG, "onReceive2: event = $event")
                GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
            }
        }


//        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
//        if (geofencingEvent != null) {
//            if (geofencingEvent.hasError()) {
//                val errorMessage = GeofenceStatusCodes
//                    .getStatusCodeString(geofencingEvent.errorCode)
//                Log.e(TAG, errorMessage)
//                return
//            }
//
//            // Get the transition type.
//            val geofenceTransition = geofencingEvent.geofenceTransition
//
//            // Test that the reported transition was of interest.
//            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER
////                || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
//            ) {
//
//                // Get the geofences that were triggered. A single event can trigger
//                // multiple geofences.
//                val triggeringGeofences = geofencingEvent.triggeringGeofences
//
//                // Get the transition details as a String.
//                val geofenceTransitionDetails = getGeofenceTransitionDetails(
//                    this,
//                    geofenceTransition,
//                    triggeringGeofences
//                )
//
//                // Send notification and log the transition details.
//                if (context != null) {
//                    if (geofenceTransitionDetails != null) {
////                        Log.i(TAG, geofenceTransitionDetails)
//                        sendNotification(context, geofenceTransitionDetails)
//                    }
//                }
//
//
//            } else {
//                // Log the error.
//                context?.let {
//                    Log.e(
//                        TAG, context.getString(
//                            R.string.geofence_transition_invalid_type,
//                            geofenceTransition
//                        )
//                    )
//                }
//
//            }
//
//
//        }


    }

//    private fun getGeofenceTransitionDetails(
//        geofenceBroadcastReceiver: GeofenceBroadcastReceiver,
//        geofenceTransition: Int,
//        triggeringGeofences: List<Geofence>?
//    ): ReminderDataItem? {
//        return if (triggeringGeofences != null) {
//            ReminderDataItem("fake","fake","fake",1.0,1.0,"fake")
//        } else
//            null
//
//    }


}