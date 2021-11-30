package com.scope.vehicletracker.network.response.vehicle


import androidx.annotation.Keep
import androidx.room.TypeConverters
import com.scope.vehicletracker.db.Converters

@Keep
@TypeConverters(Converters::class)
data class VehicleResponse(
    val `data`: List<Data?>?,
    val message: String?
) {
    @Keep
    @TypeConverters(Converters::class)
    data class Data(
        val lat: Double?,
        val lon: Double?,
        val vehicleid: Int?
    )
}