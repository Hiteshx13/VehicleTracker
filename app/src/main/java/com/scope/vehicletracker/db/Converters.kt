package com.scope.vehicletracker.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.scope.vehicletracker.network.response.owner.OwnerResponse

class Converters {

    @TypeConverter
    fun fromOwner(owner: OwnerResponse.Data.Owner): String {
        return Gson().toJson(owner)
    }

    @TypeConverter
    fun toOwner(strOwner: String): OwnerResponse.Data.Owner {
        return Gson().fromJson(strOwner, OwnerResponse.Data.Owner::class.java)

    }

    @TypeConverter
    fun fromVehicles(vehicles: MutableList<OwnerResponse.Data.Vehicle>): String {
        return Gson().toJson(vehicles)
    }

    @TypeConverter
    fun toVehicles(strVehicles: String): MutableList<OwnerResponse.Data.Vehicle> {
        val listType=object: TypeToken<MutableList<OwnerResponse.Data.Vehicle>>(){}.type
        return Gson().fromJson(strVehicles,listType)
    }
}