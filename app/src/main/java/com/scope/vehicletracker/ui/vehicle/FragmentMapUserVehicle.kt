package com.scope.vehicletracker.ui.vehicle

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.scope.vehicletracker.R
import com.scope.vehicletracker.network.response.owner.OwnerResponse
import com.scope.vehicletracker.ui.VehicleTrackerActivity
import com.scope.vehicletracker.ui.owner.OwnerViewModel
import com.scope.vehicletracker.util.AppUtils
import com.scope.vehicletracker.util.SettingsDialogClickListener

class FragmentMapUserVehicle : Fragment(R.layout.fragment_map_user_vehicle),
    OnMapReadyCallback {

    private lateinit var viewModel: OwnerViewModel
    private var isLocationAllowed = false
    private val args: FragmentMapUserVehicleArgs by navArgs()
    private lateinit var ownerData: OwnerResponse.Data
    private var googleMap: GoogleMap? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var correntDeviceLocation: Location

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ownerData = args.ownerData
        viewModel = (activity as VehicleTrackerActivity).viewModel
        setupLocationCallback()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        setupMap()
        requestLocationPermission()

    }

    override fun onResume() {
        super.onResume()
        if (isLocationAllowed) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.d("Location Update", "${location.latitude} ${location.longitude}")
                    correntDeviceLocation = location
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