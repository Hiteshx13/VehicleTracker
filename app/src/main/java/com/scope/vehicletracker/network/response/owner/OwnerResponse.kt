package com.scope.vehicletracker.network.response.owner

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.scope.vehicletracker.db.Converters
import java.io.Serializable

@Keep
data class OwnerResponse(
    val `data`: List<Data?>?
):Serializable {
    @Entity(
        tableName = "OwnerData"
    )
    @Keep
    @TypeConverters(Converters::class)
    data class Data(
        @PrimaryKey(autoGenerate = true)
        var id: Int?,

        val owner: Owner?,
        val userid: Int?,

        val vehicles: MutableList<Vehicle>?
    ):Serializable  {
        @Keep
        @TypeConverters(Converters::class)
        data class Owner(
            val foto: String?,
            val name: String?,
            val surname: String?
        ):Serializable  {
            fun getFullName(): String {
                return "$name $surname"
            }
        }

        @Keep
        @TypeConverters(Converters::class)
        data class Vehicle(
            val color: String?,
            val foto: String?,
            val make: String?,
            val model: String?,
            val vehicleid: Int?,
            val vin: String?,
            val year: String?
        ):Serializable
    }
}