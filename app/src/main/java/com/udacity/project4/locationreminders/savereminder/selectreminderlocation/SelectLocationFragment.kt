package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
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
import com.udacity.project4.utils.*
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private var marker: Marker? = null

    private val TAG = SelectLocationFragment::class.java.simpleName
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by activityViewModel()
    private lateinit var binding: FragmentSelectLocationBinding


    private lateinit var map: GoogleMap

    private val requestLocationPermissionLauncher =
        registerForeground({ setCurrentLocation() }, { displayLocationRationale() })

    private lateinit var mapFragment: SupportMapFragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        _viewModel.mapReady.postValue(false)
        _viewModel.saveProgressing = false

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setHasOptionsMenu(true)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this


        _viewModel.locationServiceEnabled.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    setCurrentLocationOnMap()
                }
            }
        }
        binding.btnSelect.setOnClickListener {
            onLocationSelected()
        }

        _viewModel.selectedPOI.observe(viewLifecycleOwner) {
            if (_viewModel.mapReady.value == true) {
                if (it != null) {
                    updateMarkerLocation(it)
                }
            }
        }

        _viewModel.mapReady.observe(viewLifecycleOwner) {
            Log.d(TAG, "Map ready $it")
            if (it) {

                // TODO: zoom to the user location after taking his permission
                checkForegroundPermissions(
                    requestLocationPermissionLauncher
                ) { setCurrentLocation() }

                // TODO: add style to the map
                setMapStyle(map)

                // TODO: put a marker to location that the user selected
                setOnMapLongClick(map)

                setPoiClick(map)

                _viewModel.selectedPOI.value?.let { updateMarkerLocation(it) }
            } else {
                Log.e(TAG, "map not ready")
            }
        }

        _viewModel.showToast.value = getString(
            R.string.select_location_educational_msg,
            getString(R.string.btn_select_location).uppercase()
        )

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
        _viewModel.reminderSelectedLocationStr.postValue(_viewModel.selectedPOI.value?.name)
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
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
        Log.d(TAG, "map ready")
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


    private fun displayLocationRationale() {
        Log.d(TAG, "displayLocationRationale")

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(
                binding.root,
                getString(R.string.foreground_educational_message),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Ok") {
                    askForForegroundPermissions(requestLocationPermissionLauncher)
                }
                .show()
        }
    }

    private fun displayLocationServicesSnackbar() {

        Snackbar.make(
            binding.btnSelect,
            R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok) {
            checkDeviceLocationSettings(requireActivity(),
                { setCurrentLocationOnMap() },
                { displayLocationServicesSnackbar() })
        }.show()
    }

    @SuppressLint("MissingPermission")
    private fun setCurrentLocationOnMap() {
        Log.d(TAG, "setCurrentLocation")
        map.isMyLocationEnabled = true

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_LOW_POWER, null)
            .addOnSuccessListener { location: Location? ->
                Log.d(TAG, "fusedLocationClient.addOnSuccessListener")
                if (location != null) {
                    Log.d(TAG, "current location : $location")
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    Log.d(TAG, "Camera will move to current location")

                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLocation,
                            15f
                        )
                    )
                } else {
                    Log.d(TAG, "current location : null")
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun setCurrentLocation() {
        checkDeviceLocationSettings(requireActivity(),
            { setCurrentLocationOnMap() },
            { displayLocationServicesSnackbar() })

    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.mapReady.postValue(false)
        _viewModel.locationServiceEnabled.postValue(false)
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()

    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "onResume")
//        checkForegroundPermissions(
//            requestLocationPermissionLauncher
//        ) { }

    }


}
