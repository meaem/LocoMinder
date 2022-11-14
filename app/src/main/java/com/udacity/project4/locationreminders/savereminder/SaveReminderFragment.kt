package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.isBackgroundLocationPermissionGranted
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.activityViewModel

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by activityViewModel()

    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

//    private lateinit var rData: ReminderDataItem

    private val TAG = SaveReminderFragment::class.java.simpleName

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.action.ACTION_GEOFENCE_EVENT"
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
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
            ActivityResultContracts.RequestMultiplePermissions()// .RequestPermission()
        ) { permissions ->
            permissions.entries.forEach {
                Log.d("DEBUG", "${it.key} = ${it.value}")
            }

            /*
            *  isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
//                checkDeviceLocationSettingsAndStartGeofence()

                showEnableLocationSetting()
                Log.d(TAG, "good, permission granted ")
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                Log.d(TAG, "Ooops, permission not granted ")
                displayLocationRationale()

            }*/
        }

    @SuppressLint("MissingPermission", "InlinedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        binding.lifecycleOwner = this


        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel



        binding.selectLocation.setOnClickListener {
            //            Navigate to SelectLocation fragment to get the user location

            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.toSelectLocationFragment())
        }


        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.saveReminder.setOnClickListener {

            if (_viewModel.validateEnteredData()) {
                if (isBackgroundLocationPermissionGranted(requireContext())) {
                    Log.d(TAG, "BackgroundLocationPermissionGranted ")
//                    checkDeviceLocationSettings()
                    showEnableLocationSetting()


                } else {

                    requestBackgroundLocationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        )
                    )

                }
            }

        }

        _viewModel.remiderSavedLocally.observe(viewLifecycleOwner) {
            if (it) {
//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
                val geo = getGeofence(_viewModel.reminderData)
                Log.d(TAG, "The following Geofence will bw added $geo")
                geofencingClient.addGeofences(getGeofencingRequest(geo), geofencePendingIntent)
                    .run {
                        addOnSuccessListener {
//                            CoroutineScope(Dispatchers.Main).launch {
//                                Log.d(TAG, getString(R.string.geofence_added))
////                            _viewModel.showToast.value = "geofence added getString(R.string.geofence_added)"
//                            }

                            Log.d(TAG, "geofence added")
                        }
                        addOnFailureListener {

                            _viewModel.deleteRemider()
//                            CoroutineScope(Dispatchers.Main).launch{
                            _viewModel.showErrorMessage.value =
                                "Could not add the Geofence because of this error : ${it.message}"
//
//                            }
                        }
                    }
            }
        }

        return binding.root
    }

//    @SuppressLint("MissingPermission", "InlinedApi")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        _viewModel.showLoading.observe(viewLifecycleOwner){
//            Log.d("BindingAdapters**",it.toString())
//        }
    }

    private fun getGeofence(rData: ReminderDataItem) =
        Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(rData.id)

            // Set the circular region of this geofence.
            .setCircularRegion(
                rData.latitude!!,
                rData.longitude!!,
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
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

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            Snackbar.make(
                binding.root,
                getString(R.string.background_educational_message),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Ok") {

                }


                .show()
        } else {
            Snackbar.make(
                binding.root,
                getString(R.string.background_open_settings_message),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Ok") {

                }


                .show()
        }

    }

//    @SuppressLint("MissingPermission")
//    private fun accessBackgroundLocation() {
//
//    }


    fun showEnableLocationSetting() {
//        activity?.let {
//        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(1000)
            .setMaxUpdates(1)
//            .build()
//            val locationRequest = LocationRequest.create()
//            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(request.build())

        val task = LocationServices.getSettingsClient(requireContext())
            .checkLocationSettings(builder.build())

        task.addOnSuccessListener { response ->
            val states = response.locationSettingsStates
            if (states != null) {
                if (states.isLocationPresent) {
                    Log.d(TAG, "Done!!")
                    _viewModel.validateAndSaveReminder()
                } else {
                    Log.d(TAG, "else 1  !!")
                }
            } else {
                Log.d(TAG, "else 2 !!")
            }
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    // Handle result in onActivityResult()
                    e.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {

                    sendEx.printStackTrace()
                }
            } else {

                Log.d(TAG, "else 3!!")
                e.printStackTrace()
            }
        }
//        }
    }

//    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
////        val locationRequest = LocationRequest.create().apply {
////            priority = LocationRequest.PRIORITY_LOW_POWER
////        }
//        val request = LocationRequest.Builder(10000)
//
////
//        val builder = LocationSettingsRequest.Builder()
//            .addLocationRequest(request.build())
//
////        val builder = LocationSettingsRequest.Builder()
////            .addLocationRequest(mLocationRequestHighAccuracy)
////            .addLocationRequest(mLocationRequestBalancedPowerAccuracy)
//
//        val settingsClient = LocationServices.getSettingsClient(requireActivity())
//
//        val task = settingsClient.checkLocationSettings(builder.build())
//
////        task.addOnFailureListener { exception ->
////            if (exception is ResolvableApiException && resolve) {
////                try {
////                    exception.startResolutionForResult(
////                        requireActivity(),
////                        REQUEST_TURN_DEVICE_LOCATION_ON
////                    )
////                } catch (sendEx: IntentSender.SendIntentException) {
////                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
////                }
////            } else {
////                Log.d(TAG, exception.toString())
////                Snackbar.make(
////                    binding.btnSelect,
////                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
////                ).setAction(android.R.string.ok) {
////                    checkDeviceLocationSettings()
////                }.show()
////            }
////        }
////
////        task.addOnCompleteListener {
////
////            if (it.isSuccessful) {
////
////            }
////        }
//
//
//        task.addOnCompleteListener {
//
//
//            try {
//                val response: LocationSettingsResponse = it.getResult(ApiException::class.java)
//                // All location settings are satisfied. The client can initialize location
//                // requests here.
//                // ...
//                Log.d(TAG,"try location")
//                _viewModel.validateAndSaveReminder()
//                Log.d(TAG,"finish try")
//
//            } catch (exception: ApiException) {
//                Log.d(TAG,"catch exception: ApiException")
//                when (exception.getStatusCode()) {
//                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
//
//                        // Location settings are not satisfied. But could be fixed by showing the
//                        // user a dialog.
//                        try {
//                            Log.d(TAG,"when LocationSettingsStatusCodes.RESOLUTION_REQUIRED ")
//
//                            // Cast to a resolvable exception.
//                            val resolvable = exception as ResolvableApiException
//                            // Show the dialog by calling startResolutionForResult(),
//                            // and check the result in onActivityResult().
//                            resolvable.startResolutionForResult(
//                                requireActivity(),
//                                REQUEST_TURN_DEVICE_LOCATION_ON
//                            )
//                        } catch (e: IntentSender.SendIntentException) {
//                            Log.d(TAG,"catch (e: IntentSender.SendIntentException) ")
//
//                            // Ignore the error.
//                        } catch (e: ClassCastException) {
//                            // Ignore, should be an impossible error.
//                            Log.d(TAG,"catch (e: ClassCastException)")
//                        }
//
//                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
//                        Log.d(TAG,"when LocationSettingsStatusCodes.RESOLUTION_REQUIRED ")
//                        // Location settings are not satisfied. However, we have no way to fix the
//                        // settings so we won't show the dialog.
////                        ...
////                        break;
//                    }
//                }
//            }
//        }
//
//
//    }

}
