package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    private val TAG = SaveReminderFragment::class.java.simpleName

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(
            requireActivity(), 0, intent,
            getPendingIntentFlags()
        )
    }

    @SuppressLint("MissingPermission")
    val requestBackgroundLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
//                checkDeviceLocationSettingsAndStartGeofence()
                Log.d(TAG, "good, permission granted ")
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                Log.d(TAG, "Ooops, permission not granted ")
                displayLocationRationale()

            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.saveReminder.setOnClickListener {

            if (isBackgroundLocationPermissionGranted()) {
                val title = _viewModel.reminderTitle.value
                val description = _viewModel.reminderDescription.value
                val location = _viewModel.reminderSelectedLocationStr.value
                val latitude = _viewModel.latitude.value
                val longitude = _viewModel.longitude.value

                val rData =
                    ReminderDataItem(title!!, description, location, latitude!!, longitude!!)
//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
                val geo = getGeofence(rData)
                geofencingClient?.addGeofences(getGeofencingRequest(geo), geofencePendingIntent)
                    ?.run {
                        addOnSuccessListener {
                            // Geofences added
                            // ...
//                        2) save the reminder to the local db
                            _viewModel.saveReminder(rData)
                        }
                        addOnFailureListener {
                            // Failed to add geofences
                            // ...
                            _viewModel.showErrorMessage.value = "Could not add the Geo fence"
                        }
                    }


            } else {
                requestBackgroundLocationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )

            }

        }


    }

    private fun getGeofence(rData: ReminderDataItem) =
        Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(rData.id)

            // Set the circular region of this geofence.
            .setCircularRegion(
                rData.latitude,
                rData.longitude,
                rData.radiusInMeters
            )

            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(Geofence.NEVER_EXPIRE)

            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

            // Create the geofence.
            .build()


    private fun getGeofencingRequest(fence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(listOf(fence))
        }.build()
    }

    private fun getPendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun displayLocationRationale() {
        Log.d(TAG, "displayLocationRationale")

//        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
        Snackbar.make(
            binding.root,
            getString(R.string.background_educational_message),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("Ok") {

            }


            .show()
//        }
    }

//    @SuppressLint("MissingPermission")
//    private fun accessBackgroundLocation() {
//
//    }

    private fun isBackgroundLocationPermissionGranted(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q
        ) {
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        } else {
            true
        }

    }


}
