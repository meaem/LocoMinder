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

    }
}