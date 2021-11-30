package com.scope.vehicletracker.ui.vehicle

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.scope.vehicletracker.R
import com.scope.vehicletracker.network.Resource
import com.scope.vehicletracker.network.response.owner.OwnerResponse
import com.scope.vehicletracker.ui.VehicleTrackerActivity
import com.scope.vehicletracker.ui.owner.OwnerViewModel
import com.scope.vehicletracker.util.AppUtils
import com.scope.vehicletracker.util.AppUtils.animateMarker
import com.scope.vehicletracker.util.Constants.Companion.VEHICLE_API_DELAY
import com.scope.vehicletracker.util.LatLngInterpolator
import com.scope.vehicletracker.util.SettingsDialogClickListener
import kotlinx.android.synthetic.main.fragment_map_user_vehicle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.akexorcist.googledirection.DirectionCallback

import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.model.Direction


class FragmentMapUserVehicle : Fragment(R.layout.fragment_map_user_vehicle),
    OnMapReadyCallback {

    private val TAG = "FragmentMapUserVehicle"
    private lateinit var viewModel: OwnerViewModel
    private var isLocationAllowed = false
    private val args: FragmentMapUserVehicleArgs by navArgs()
    private lateinit var ownerData: OwnerResponse.Data
    private var googleMap: GoogleMap? = null
    private var mapMarkers = HashMap<String?, Marker?>()
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentDeviceLocation: Location
    private var oldVehicleList: List<OwnerResponse.Data.Vehicle>? = null
    private var isUpdating = false
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ownerData = args.ownerData
        oldVehicleList = ownerData.vehicles
        viewModel = (activity as VehicleTrackerActivity).viewModel
        setupMap()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        setupLocationCallback()
        requestLocationPermission()

        setObserver()
        callApiEveryMinute()

        if(isLocationAllowed){
            getDirectionToNearestVehicle()
        }
    }


    private fun callApiEveryMinute() {
        CoroutineScope(Main).launch {
            viewModel.getVehicleDataFromAPI(ownerData.userid.toString())
            Log.d("callApiEveryMinute", "call")
            delay(VEHICLE_API_DELAY)
            callApiEveryMinute()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        isUpdating = false
    }

    override fun onResume() {
        super.onResume()
        if (isLocationAllowed) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun setObserver() {
        viewModel.vehicleResponseAPI.observe(viewLifecycleOwner, Observer { response ->

            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { ownerResponse ->
                        ownerResponse.data?.forEach { newVehicleResponse ->
                            oldVehicleList?.forEach { oldVehicleData ->
                                if (oldVehicleData.vehicleid?.equals(newVehicleResponse?.vehicleid) == true) { // Removing empty data from response
                                    oldVehicleData.lat = newVehicleResponse?.lat
                                    oldVehicleData.lon = newVehicleResponse?.lon
                                    showOwnerVehicleOnMap(oldVehicleData)
                                }
                            }

                        }
                        zoomOnFirstMarker()
                        updateVehicleDataInDB()
                        isUpdating = true


                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
                        Log.d(TAG, "Error occured: $message ")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    /**
     * get route between current location to nearest vehicle
     */
    private fun getDirectionToNearestVehicle(){
        val serverKey = "AIzaSyAvp9SJ6mZIcyxxhT2MrvhLjUrHjMGscAA"
        val origin = LatLng(37.7849569, -122.4068855)
        val destination = LatLng(37.7814432, -122.4460177)
        GoogleDirection.withServerKey(serverKey)
            .from(origin)
            .to(destination)
            .execute(object : DirectionCallback {

                override fun onDirectionSuccess(direction: Direction?) {
                    Log.d("","")
                }

                override fun onDirectionFailure(t: Throwable) {
                    // Do something here
                    Log.d("","")
                }
            })
    }

    private fun hideProgressBar() {
        progressBarMap.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        progressBarMap.visibility = View.VISIBLE
    }

    private fun showOwnerVehicleOnMap(vehicle: OwnerResponse.Data.Vehicle) {

        if (isUpdating) {
            val marker = mapMarkers[vehicle.vehicleid.toString()]
            animateMarker(
                marker!!, LatLng(
                    vehicle.lat!!,
                    vehicle.lon!!
                ), LatLngInterpolator.Spherical()
            )
        } else {
            val marker: Marker?
            googleMap?.setInfoWindowAdapter(
                CustomInfoWindowVehicleAdapter(
                    layoutInflater,
                    isUpdating
                )
            )

            val markerOpt = MarkerOptions().position(
                LatLng(
                    vehicle.lat!!,
                    vehicle.lon!!
                )
            ).snippet(vehicle.model)
            marker = googleMap?.addMarker(markerOpt)

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
//            markerList.add()
        }
    }

    fun updateVehicleDataInDB(){
        ownerData.vehicles=oldVehicleList
        viewModel.updateOwnerVehicleData(ownerData)
    }

    /** zoom on first marker*/
    private fun zoomOnFirstMarker() {
//        Log.d("old",""+Pld)
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

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.d("Location Update", "${location.latitude} ${location.longitude}")
                    currentDeviceLocation = location
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }


    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun requestLocationPermission() {
        val requestPermisisionLauncher =
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

        when {
            ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION

            )
                    == PackageManager.PERMISSION_GRANTED -> {
                googleMap?.isMyLocationEnabled = true
                startLocationUpdates()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // Message
            }
            else -> {
                requestPermisisionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
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

    private fun setupMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }
}