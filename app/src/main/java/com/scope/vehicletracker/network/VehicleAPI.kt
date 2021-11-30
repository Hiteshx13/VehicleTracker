package com.scope.vehicletracker.network

import com.scope.vehicletracker.network.response.owner.OwnerResponse
import com.scope.vehicletracker.network.response.vehicle.VehicleResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VehicleAPI {

    @GET("/api/")
    suspend fun getOwnerData(
        @Query("op")
        op: String
    ): Response<OwnerResponse>

    @GET("/api/")
    suspend fun getVehicleData(
        @Query("op")
        op: String,
        @Query("userid")
        userid: String
    ): Response<Any>


}