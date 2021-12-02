package com.scope.tracker.ui.vehicle

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.scope.tracker.R
import com.scope.tracker.network.Resource
import com.scope.tracker.ui.VehicleTrackerActivity
import com.scope.tracker.ui.VehicleTrackerViewModel
import com.scope.tracker.ui.owner.OwnerResponse
import com.scope.tracker.util.AppUtils
import com.scope.tracker.util.AppUtils.calculateDistance
import com.scope.tracker.util.AppUtils.isInternetAvailable
import com.scope.tracker.util.AppUtils.showToast
import com.scope.tracker.util.Constants.Companion.INTERVAL_DEVICE_LOCATION
import com.scope.tracker.util.Constants.Companion.VEHICLE_API_DELAY
import com.scope.tracker.util.LatLngInterpolator
import com.scope.tracker.util.SettingsDialogClickListener
import kotlinx.android.synthetic.main.fragment_map_user_vehicle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class FragmentMapUserVehicle : Fragment(R.layout.fragment_map_user_vehicle),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var viewModel: VehicleTrackerViewModel
    private var isLocationAllowed = false
    private val args: FragmentMapUserVehicleArgs by navArgs()
    private lateinit var ownerData: OwnerResponse.Data
    private var googleMap: GoogleMap? = null
    private var mapMarkers = HashMap<String?, Marker?>()
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentDeviceLocation: Location
    private var oldVehicleList: List<OwnerResponse.Data.Vehicle>? = null
    private var isUpdatingVehicleLocation = false
    private var markerClickID = ""
    private lateinit var job: Job
    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var polylineNearesrtVehicle: Polyline? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ownerData = args.ownerData
        oldVehicleList = ownerData.vehicles

        initPermissionLauncher()
        setupGoogleMap()
        setupDeviceLocationCallback()
        setObserver()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isUpdatingVehicleLocation = false
        viewModel = (activity as VehicleTrackerActivity).viewModel
    }

    override fun onDetach() {
        super.onDetach()
        job.cancel()
        isUpdatingVehicleLocation = false
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    fun initPermissionLauncher() {
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        isLocationAllowed = true
                        googleMap?.isMyLocationEnabled = true
                        startLocationUpdates()
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        isLocationAllowed = true
                        googleMap?.isMyLocationEnabled = true
                        startLocationUpdates()
                    }
                    else -> {
                        isLocationAllowed = false
                        showSettingsDialog()
                    }
                }
            }
    }

    private fun setupGoogleMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun callApiEveryMinute() {
        job = CoroutineScope(Main).launch {
            if (isInternetAvailable(requireContext())) {
                viewModel.getVehicleDataFromAPI(ownerData.userid.toString())
            } else {
                progressBarMap.visibility = View.GONE
                requireContext().showToast(getString(R.string.please_check_device_internet_connection))
            }

            delay(VEHICLE_API_DELAY)
            callApiEveryMinute()
        }
    }

    private fun setObserver() {
        viewModel.vehicleResponseAPI.observe(viewLifecycleOwner, { response ->

            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { ownerResponse ->
                        ownerResponse.data?.forEach { newVehicleResponse ->
                            /** checking for null location**/
                            if (newVehicleResponse?.lat != null
                                && newVehicleResponse.lon != null
                            ) {
                                oldVehicleList?.forEach { oldVehicleData ->
                                    if (oldVehicleData.vehicleid?.equals(newVehicleResponse.vehicleid) == true
                                    ) { // Removing empty data from response
                                        oldVehicleData.lat = newVehicleResponse.lat
                                        oldVehicleData.lon = newVehicleResponse.lon

                                        if (isUpdatingVehicleLocation) {
                                            updateVehiclePosition()
                                        } else {
                                            showOwnerVehicleOnMap()
                                        }
                                    }
                                }
                            }
                        }
                        updateVehicleDataInDB()
                        if (!isUpdatingVehicleLocation) {
                            isUpdatingVehicleLocation = true
                            zoomOnFirstMarker()
                        }
                        isUpdatingVehicleLocation = true
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        requireContext().showToast(message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    /** zoom on first marker*/
    private fun zoomOnFirstMarker() {
        if (!oldVehicleList.isNullOrEmpty()) {
            val firstVehicle = oldVehicleList!![0]

            if (firstVehicle.lat != null && firstVehicle.lon != null) {
                val zoomLatLan = LatLng(
                    firstVehicle.lat!!,
                    firstVehicle.lon!!
                )

                val cameraPosition =
                    CameraPosition.Builder().target(zoomLatLan).zoom(16.0f).build()
                val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
                googleMap?.moveCamera(cameraUpdate)
            }
        }
    }

    private fun hideProgressBar() {
        progressBarMap.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        progressBarMap.visibility = View.VISIBLE
    }

    /**
     * showing vehicle on map
     * */
    private fun showOwnerVehicleOnMap() {
        googleMap?.setInfoWindowAdapter(CustomInfoWindowVehicleAdapter(layoutInflater))
        googleMap?.setOnMarkerClickListener(this)

        oldVehicleList?.forEach { vehicle ->

            if (vehicle.lat != null && vehicle.lon != null) {
                val markerOpt = MarkerOptions().position(
                    LatLng(
                        vehicle.lat!!,
                        vehicle.lon!!
                    )
                ).snippet(vehicle.model)
                val marker = googleMap?.addMarker(markerOpt)

                marker?.tag = vehicle
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLng(
                        LatLng(
                            vehicle.lat!!,
                            vehicle.lon!!
                        )
                    )
                )

                mapMarkers[vehicle.vehicleid.toString()] = marker
            }
        }
    }

    /**
     * updating vehicle position on map
     * */
    private fun updateVehiclePosition() {

        oldVehicleList?.forEach { vehicle ->

            for ((key, marker) in mapMarkers) {

                if (key.toString().equals(vehicle.vehicleid.toString(), true)) {
                    val vehicleOldData: OwnerResponse.Data.Vehicle =
                        marker?.tag as OwnerResponse.Data.Vehicle
                    vehicleOldData.lat = vehicle.lat
                    vehicleOldData.lon = vehicle.lon
                    marker.tag = vehicleOldData

                    AppUtils.animateMarker(
                        marker, LatLng(
                            vehicle.lat!!,
                            vehicle.lon!!
                        ), LatLngInterpolator.Spherical()
                    )
                }
            }
        }
        showNearestVehiclePolyLine()
        /** updating current open infoWindow data **/
        if (markerClickID.isNotEmpty()) {
            val markerClicked = mapMarkers[markerClickID]
            if (markerClicked?.isInfoWindowShown == true) {
                markerClicked.showInfoWindow()
            }
        }
    }

    private fun updateVehicleDataInDB() {
        ownerData.vehicles = oldVehicleList
        viewModel.updateOwnerVehicleData(ownerData)
    }

    private fun setupDeviceLocationCallback() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.d("Location Update", "${location.latitude} ${location.longitude}")
                    currentDeviceLocation = location
                    showNearestVehiclePolyLine()
                }
            }
        }
    }

    /** start location update*/
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = INTERVAL_DEVICE_LOCATION
        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    /** stop location update*/
    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                googleMap?.isMyLocationEnabled = true
                isLocationAllowed = true
                startLocationUpdates()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showSettingsDialog()
            }
            else -> {
                requestPermissionLauncher?.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    fun showNearestVehiclePolyLine() {
        if (isLocationAllowed) {


            /** finding shortest distance for available vehicle form current device location**/
            var nearestVehicle: OwnerResponse.Data.Vehicle? = null

            var shortestDistance = 0.0f

            val locationDevice = Location(currentDeviceLocation)
            val locationVehicle = Location("vehicle")
            oldVehicleList?.forEach {

                if (it.lat != null && it.lon != null) {
                    locationVehicle.latitude = it.lat!!
                    locationVehicle.longitude = it.lon!!

                    val distance = calculateDistance(locationDevice, locationVehicle)

                    if (shortestDistance ==0.0 || distance < shortestDistance) {
                        shortestDistance = distance
                        nearestVehicle = it
                    }
                }
            }

            /** remove old polyline and add new polyline with
             * updated position of vehicle **/
            if (nearestVehicle?.lat != null && nearestVehicle?.lon != null) {
                polylineNearesrtVehicle?.remove()
                polylineNearesrtVehicle = googleMap?.addPolyline(
                    PolylineOptions()
                        .clickable(true)
                        .color(Color.BLUE)
                        .add(
                            LatLng(currentDeviceLocation.latitude, currentDeviceLocation.longitude),
                            LatLng(nearestVehicle?.lat!!, nearestVehicle?.lon!!)
                        )
                )
            }
        }
    }

    /**
     * show dialog to grant location  permission from application settings
     */
    private fun showSettingsDialog() {
        AppUtils.showDialog(activity!!, object : SettingsDialogClickListener {
            override fun onSettingClicked() {
                AppUtils.openAppSettings(activity!!)
            }
        })
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        requestLocationPermission()
        callApiEveryMinute()
    }

    /** function for Map Marker click listener**/
    override fun onMarkerClick(marker: Marker): Boolean {
        val model: OwnerResponse.Data.Vehicle = marker.tag as OwnerResponse.Data.Vehicle
        markerClickID = model.vehicleid.toString()
        return false
    }
}
