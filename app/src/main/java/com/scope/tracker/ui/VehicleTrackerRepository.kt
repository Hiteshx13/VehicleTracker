package com.scope.tracker.ui

import com.scope.tracker.db.OwnerDatabase
import com.scope.tracker.network.RetrofitInstance
import com.scope.tracker.ui.owner.OwnerResponse

class VehicleTrackerRepository(
    val db: OwnerDatabase
) {
    /** get owner list from api*/
    suspend fun getOwnerDataFromAPI(op: String) = RetrofitInstance.api.getOwnerData(op)

    /** get vehicle data from api*/
    suspend fun getVehicleDataFromAPI(op: String, userID: String) =
        RetrofitInstance.api.getVehicleData(op, userID)

    /** insert owner data into local storage*/
    suspend fun upsert(ownerData: OwnerResponse.Data) = db.getOwnerDao().upsert(ownerData)

    /** get owner data from local storage*/
    fun getOwnerDataFromDB() = db.getOwnerDao().getOwnerDataFromDB()

    /** remove all owner data from local storage*/
    suspend fun deleteAllRecords() = db.getOwnerDao().deleteAllRecords()
}