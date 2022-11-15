package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.udacity.project4.utils.*
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by activityViewModel()

    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
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

    private fun displayLocationServicesSnackbar() {

        Snackbar.make(
            binding.selectLocation,
            R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok) {
            checkDeviceLocationSettings(requireActivity(),
                { save() },
                { displayLocationServicesSnackbar() })
        }.show()
    }

    private fun save() {
//        _viewModel.saveProgressing = true

        checkDeviceLocationSettings(requireActivity(),
            { _viewModel.validateAndSaveReminder() },
            { displayLocationServicesSnackbar() }
        )
    }

    private val requestAllPermissionLauncher =
        registerAll({ save() }, { displayLocationRationale() })


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
                checkAllPermissions(requestAllPermissionLauncher) { save() }
            }

        }

        _viewModel.remiderSavedLocally.observe(viewLifecycleOwner) {
            if (it) {
                val geo = getGeofence(_viewModel.reminderData)
                Log.d(TAG, "The following Geofence will bw added $geo")
                geofencingClient.addGeofences(getGeofencingRequest(geo), geofencePendingIntent)
                    .run {
                        addOnSuccessListener {
                            Log.d(TAG, "geofence added")
                        }
                        addOnFailureListener {

                            _viewModel.deleteRemider()
                            _viewModel.showErrorMessage.value =
                                getString(R.string.err_could_not_add_geofence, it.message)
                        }
                    }
            }
        }

//        _viewModel.locationServiceEnabled.observe(viewLifecycleOwner) {
//            it?.let {
//                if (_viewModel.saveProgressing) {
//                    _viewModel.validateAndSaveReminder()
//                }
//            }
//        }

        return binding.root
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
                .setAction("Ok") {}
                .show()
        } else {
            Snackbar.make(
                binding.root,
                getString(R.string.background_open_settings_message),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Ok") {}
                .show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult from Fragment")
        when (requestCode) {
            REQUEST_TURN_DEVICE_LOCATION_ON -> when (resultCode) {
                Activity.RESULT_OK -> _viewModel.validateAndSaveReminder()
                else -> {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.err_could_not_save_geofence),
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction("Ok") {}
                        .show()
                }

            }
        }
    }

}
