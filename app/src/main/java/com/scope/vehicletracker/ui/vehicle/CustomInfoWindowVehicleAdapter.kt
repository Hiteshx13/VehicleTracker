package com.scope.vehicletracker.ui.vehicle

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import com.scope.vehicletracker.R
import com.scope.vehicletracker.databinding.RowInfoWindowVehicleBinding
import com.scope.vehicletracker.network.response.owner.OwnerResponse.Data.Vehicle
import com.scope.vehicletracker.util.AppUtils
import com.scope.vehicletracker.util.AppUtils.getAddressFromLatLan
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

internal class CustomInfoWindowVehicleAdapter(
    private var inflater: LayoutInflater,
    private var isUpdating: Boolean
) :
    InfoWindowAdapter {

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    @SuppressLint("SetTextI18n")
    override fun getInfoWindow(marker: Marker): View {
        val binding = DataBindingUtil.inflate<RowInfoWindowVehicleBinding>(
            inflater,
            R.layout.row_info_window_vehicle,
            null,
            false
        )

        val model: Vehicle = marker.tag as Vehicle
        binding.tvAddress.text =
            getAddressFromLatLan(binding.tvName.context, model.lat!!, model.lon!!)
                .replace(",", "\n")

        binding.tvName.text = "${model.make} ${model.model}"

        val color = Color.parseColor(model.color)
        binding.viewVehicleColor.setBackgroundColor(color)

        Log.d("VEHICLE_IMAGE", model.foto ?: "")
        /** removing extra string from image url and loading image**/
        val formattedImageUrl = AppUtils.getFormattedImageUrl(model.foto.toString())
        Picasso.get().load(formattedImageUrl)
            .placeholder(R.drawable.ic_user)
            .into(
                binding.ivProfile,
                object : MarkerCallback(marker) {

                },
            )
        return binding.root
    }

    internal open class MarkerCallback(private var marker: Marker?) :
        Callback {

        override fun onSuccess() {
            if (marker == null) {
                return
            }
            if (!marker!!.isInfoWindowShown) {
                return
            }

            marker!!.hideInfoWindow()
            /** Calling only showInfoWindow() throws an error */
            marker!!.showInfoWindow()
        }

        override fun onError(e: java.lang.Exception?) {}
    }
}