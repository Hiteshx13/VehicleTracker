package com.scope.tracker.ui.vehicle

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import com.scope.tracker.R
import com.scope.tracker.databinding.RowInfoWindowVehicleBinding
import com.scope.tracker.ui.owner.OwnerResponse.Data.Vehicle
import com.scope.tracker.util.AppUtils
import com.scope.tracker.util.AppUtils.getAddressFromLatLan
import com.scope.tracker.util.AppUtils.isInternetAvailable
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

internal class CustomInfoWindowVehicleAdapter(
    private var inflater: LayoutInflater
) :
    InfoWindowAdapter {

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View {
        val binding = DataBindingUtil.inflate<RowInfoWindowVehicleBinding>(
            inflater,
            R.layout.row_info_window_vehicle,
            null,
            false
        )
        val model: Vehicle = marker.tag as Vehicle

        if(isInternetAvailable(binding.root.context)&&model.lat!=null && model.lon!=null){
            binding.tvAddress.text =
                getAddressFromLatLan(binding.tvName.context, model.lat!!, model.lon!!)
                    .replace(",", "\n")
        }

        binding.tvName.text = "${model.make} ${model.model}"
        val color = Color.parseColor(model.color)
        binding.viewVehicleColor.setBackgroundColor(color)

        /** removing extra string from image url and loading image**/
        val formattedImageUrl = AppUtils.getFormattedImageUrl(model.foto.toString())
        Log.d("VehicleImage","$formattedImageUrl")
        Picasso.get().load(formattedImageUrl)
            .placeholder(R.drawable.ic_car)
            .into(
                binding.ivVehicle,
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