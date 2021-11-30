package com.scope.vehicletracker.ui.owner

import com.scope.vehicletracker.db.OwnerDatabase
import com.scope.vehicletracker.network.RetrofitInstance
import com.scope.vehicletracker.network.response.owner.OwnerResponse

class OwnerRepository(
    val db: OwnerDatabase
) {
    /** get owner list from api*/
    suspend fun getOwnerDataFromAPI(op: String) = RetrofitInstance.api.getOwnerData(op)

    /** get vehicle data from api*/
    suspend fun getVehicleDataFromAPI(op: String, userID: String) =
        RetrofitInstance.api.getVehicleData(op, userID)

    /** insert owner data into local storage*/
    suspend fun upsert(ownerData: OwnerResponse.Data) = db.getOwnerDao().upsert(ownerData)
    suspend fun updateVehicleData(ownerData: OwnerResponse.Data) = db.getOwnerDao().updateVehicleData(ownerData)

    /** get owner data from local storage*/
    fun getOwnerDataFromDB() = db.getOwnerDao().getOwnerDataFromDB()

    /** remove all owner data from local storage*/
    suspend fun deleteAllRecords() = db.getOwnerDao().deleteAllRecords()
}