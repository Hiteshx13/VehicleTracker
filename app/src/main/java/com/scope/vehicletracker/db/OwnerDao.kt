package com.scope.vehicletracker.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.scope.vehicletracker.network.response.owner.OwnerResponse

@Dao
interface OwnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(owner: OwnerResponse.Data): Long

    @Query("SELECT * FROM  ownerdata")
    fun getAllOwners(): LiveData<List<OwnerResponse.Data>>

    @Query("DELETE FROM  ownerdata")
    suspend fun deleteAllRecords()

}