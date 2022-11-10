package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private var marker: Marker? = null

    private val TAG = SelectLocationFragment::class.java.simpleName
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding


    private lateinit var map: GoogleMap


//    override val _viewModel: SelectLocationViewModel by viewModel()

    // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher. You can use either a val, as shown in this snippet,
// or a lateinit var in your onAttach() or onCreate() method.
    @SuppressLint("MissingPermission")
    val requestLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in the app.
                setCurrentLocation()
                Log.d(TAG, "good, permission granted ")
            } else {

                Log.d(TAG, "Ooops, permission not granted ")
                //show educational message if necessary
                displayLocationRationale()

            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        _viewModel.mapReady.postValue(false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setHasOptionsMenu(true)


//        TODO: add the map setup implementation

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

//        _viewModel.navigationCommand.observe(viewLifecycleOwner) {
//
//        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        binding.btnSelect.setOnClickListener {

            // TODO: call this function after the user confirms on the selected location
//            _viewModel.showErrorMessage.value ="here"
//            if (marker != null) {
            Log.d(TAG, "btnSelect.setOnClickListener")
//                _viewModel.showErrorMessage.value ="no error"
            onLocationSelected()

//            } else {
//                Log.d(TAG, "null marker")
//                _viewModel.showErrorMessage.value =
//                    "Please select a place by either click on any landmark or by long click on any desired location"

//            }
        }

        _viewModel.reminderSelectedLocationStr.observe(viewLifecycleOwner) {
            binding.btnSelect.isEnabled = it != null
        }
        _viewModel.selectedPOI.observe(viewLifecycleOwner) {
            if (_viewModel.mapReady.value == true) {

                if (it != null) {
                    _viewModel.reminderSelectedLocationStr.postValue(it.name)
                    updateMarkerLocation(it)
                }
            }


//            if (::map.isInitialized ) {
//                if (it != null) {
//
//                }
//            }
        }

        _viewModel.mapReady.observe(viewLifecycleOwner) {

            if (it) {
                // TODO: zoom to the user location after taking his permission
                checkPermissions()

                // TODO: add style to the map
                setMapStyle(map)

                // TODO: put a marker to location that the user selected
                setOnMapLongClick(map)

                setPoiClick(map)

//        androidOverlay.position(homeLocation, 100f)
//        map.addGroundOverlay(androidOverlay)

                _viewModel.selectedPOI.value?.let { updateMarkerLocation(it) }
                EspressoIdlingResource.decrement()
            } else {
                binding.btnSelect.isEnabled = false
                EspressoIdlingResource.increment()
            }

        }
//

//        _viewModel.showSnackBar.value = getString(
//            R.string.select_location_educational_msg,
//            getString(R.string.btn_select_location).uppercase()
//        )
//        _viewModel.showToast.call()


    }


    private fun updateMarkerLocation(it: PointOfInterest) {
        marker?.remove()

        marker = map.addMarker(
            MarkerOptions()
                .position(it.latLng)
                .title(it.name)
                .snippet(it.name)

                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        marker?.showInfoWindow()
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence

//        _viewModel.longitude.value = marker?.position?.longitude
//        _viewModel.latitude.value = marker?.position?.latitude
//        _viewModel.reminderSelectedLocationStr.postValue(marker?.title)
        Log.d(TAG, "xlocation: " + _viewModel.selectedPOI.value?.name)
        Log.d(TAG, "xLocationStr: " + _viewModel.reminderSelectedLocationStr.value)

        _viewModel.navigationCommand.postValue(NavigationCommand.Back)


//        findNavController().popBackStack()
//        findNavController().navigate(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        _viewModel.mapReady.postValue(true)


    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.map_style)
            ).also {
                if (!it) {
                    Log.e(TAG, "Style parsing failed.")
                }
            }

        } catch (ex: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", ex)
        }

    }


    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener {
            _viewModel.selectedPOI.postValue(it)
        }
    }

    private fun setOnMapLongClick(map: GoogleMap) {

        map.setOnMapLongClickListener {
            val snippet = getString(
                R.string.lat_long_snippet, it.latitude,
                it.longitude
            )
            _viewModel.selectedPOI.postValue(PointOfInterest(it, "", snippet))
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun displayLocationRationale() {
        Log.d(TAG, "displayLocationRationale")

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(
                binding.root,
                getString(R.string.foreground_educational_message),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Ok") {
                    requestLocationPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }


                .show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkPermissions() {
        if (isLocationPermissionGranted()) {
            setCurrentLocation()
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestLocationPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun setCurrentLocation() {


        map.setMyLocationEnabled(true)

//            fusedLocationClient.lastLocation
//                .addOnSuccessListener {
//                    if (it != null) {
//                        val currentLocation = LatLng(it.latitude, it.longitude)
//
//                        map.addMarker(
//                            MarkerOptions().position(currentLocation).title("Current Location")
//                        )
//                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
//                    } else {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_LOW_POWER, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)

//                    map.addMarker(
//                        MarkerOptions().position(currentLocation)
//                            .title("Current Location")
//                    )
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLocation,
                            15f
                        )
                    )
                }
            }
//                    }
//                }
    }


}
