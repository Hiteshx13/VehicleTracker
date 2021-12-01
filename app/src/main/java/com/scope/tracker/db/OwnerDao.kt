package com.scope.tracker.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.scope.tracker.ui.owner.OwnerResponse

@Dao
interface OwnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(owner: OwnerResponse.Data): Long

    @Query("SELECT * FROM  OwnerData")
    fun getOwnerDataFromDB(): LiveData<List<OwnerResponse.Data>>

    @Update
    fun updateVehicleData(owner: OwnerResponse.Data)

    @Query("DELETE FROM  OwnerData")
    suspend fun deleteAllRecords()

}