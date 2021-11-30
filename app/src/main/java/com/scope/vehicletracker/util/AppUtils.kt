package com.scope.vehicletracker.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.scope.vehicletracker.R
import com.scope.vehicletracker.util.Constants.Companion.PREF_NAME
import com.scope.vehicletracker.util.Constants.Companion.SAVED_DATE
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


object AppUtils {

    fun Context.showToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    /**
     * open application settings
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    /**
     * show dialog to grant location  permission from application settings
     */
    fun showDialog(mContext: Context, listener: SettingsDialogClickListener) {
        val builder = AlertDialog.Builder(mContext)
        builder.setMessage(R.string.message_location_permission)
            .setPositiveButton(
                R.string.settings
            ) { dialog, id ->
                listener.onSettingClicked()
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.cancel
            ) { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    /**
     * remove extra string from image url
     * **/
    fun getFormattedImageUrl(str: String): String {
        val indexEnd: Int
        val strUrl = str.lowercase()
        var subStrUrl = strUrl.subSequence(0, strUrl.length)
        if (strUrl.contains(".jpg", true)) {
            indexEnd = strUrl.indexOf(".jpg")
            subStrUrl = strUrl.substring(0, indexEnd + 4)
        } else if (strUrl.contains(".jpeg", true)) {
            indexEnd = strUrl.indexOf(".jpeg")
            subStrUrl = strUrl.substring(0, indexEnd + 5)
        }
        return subStrUrl.toString()
    }


    /** get current date**/
    fun getDeviceCurrentDate(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return current.format(formatter)
    }

    /** save current date in shared pref**/
    fun saveCurrentDateToPref(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var editor = sharedPref.edit()

        editor.putString(SAVED_DATE, getDeviceCurrentDate())
        editor.apply()
    }

    /** get saved date from shared pref**/
    fun getSavedDatefromPref(context: Context): String {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(SAVED_DATE, "") ?: ""

    }


    /** get address from LatLan**/
    fun getAddressFromLatLan(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String {
        val addresses: List<Address>
        val geocoder = Geocoder(context, Locale.getDefault())
        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        )

        return addresses[0]
            .getAddressLine(0)
    }


    /** Animate marker between two locations**/
    fun animateMarker(
        marker: Marker,
        finalPosition: LatLng,
        latLngInterpolator: LatLngInterpolator
    ) {
        val startPosition = marker.position
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val interpolator: Interpolator = AccelerateDecelerateInterpolator()
        val durationInMs = 2000f
        handler.post(object : Runnable {
            var elapsed: Long = 0
            var t = 0f
            var v = 0f
            override fun run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start
                t = elapsed / durationInMs
                v = interpolator.getInterpolation(t)
                marker.position = latLngInterpolator.interpolate(v, startPosition, finalPosition)
                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    /** check internet connectivity**/
    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

        return isConnected
    }

    fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection

        // Returns a Network object corresponding to
        // the currently active default data network.
        val network = connectivityManager.activeNetwork ?: return false

        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // Indicates this network uses a Wi-Fi transport,
            // or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // Indicates this network uses a Cellular transport. or
            // Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // else return false
            else -> false
        }

    }


}