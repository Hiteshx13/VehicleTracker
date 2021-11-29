package com.scope.vehicletracker.ui.owner

import androidx.lifecycle.MutableLiveData
import com.scope.vehicletracker.db.OwnerDatabase
import com.scope.vehicletracker.network.RetrofitInstance
import com.scope.vehicletracker.network.response.owner.OwnerResponse

class OwnerRepository(
    val db:OwnerDatabase
) {
    suspend fun getOwnerDataFromAPI(op: String) = RetrofitInstance.api.getOwnerList(op)
    suspend fun upsert(ownerData: OwnerResponse.Data)= db.getOwnerDao().upsert(ownerData)
    fun getOwnerDataFromDB()=db.getOwnerDao().getOwnerDataFromDB()
//    suspend fun getFirstOwners()=db.getOwnerDao().getFirstOwners()
    suspend fun deleteAllRecords()=db.getOwnerDao().deleteAllRecords()
}