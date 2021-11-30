package com.scope.vehicletracker.network.response.vehicle


import androidx.annotation.Keep
import androidx.room.TypeConverters
import com.scope.vehicletracker.db.Converters
import java.io.Serializable

@Keep
@TypeConverters(Converters::class)
data class VehicleResponse(
    val `data`: List<Data?>?,
    val message: String?
):Serializable {
    @Keep
    @TypeConverters(Converters::class)
    data class Data(
        val lat: Double?,
        val lon: Double?,
        val vehicleid: Int?
    )
}