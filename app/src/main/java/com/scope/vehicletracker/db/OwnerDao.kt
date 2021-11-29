package com.scope.vehicletracker.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.scope.vehicletracker.network.response.owner.OwnerResponse

@Dao
interface OwnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(owner: OwnerResponse.Data): Long

    @Query("SELECT * FROM  OwnerData")
    fun getOwnerDataFromDB(): LiveData<List<OwnerResponse.Data>>

//    @Query("SELECT * FROM  OwnerData LIMIT 1")
//    suspend fun getFirstOwners(): List<OwnerResponse.Data>

    @Query("DELETE FROM  OwnerData")
    suspend fun deleteAllRecords()

}