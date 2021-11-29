package com.scope.vehicletracker.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.scope.vehicletracker.network.response.owner.OwnerResponse

@Dao
interface OwnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(owner: OwnerResponse.Data):Long

    @Query("SELECT * FROM  ownerdata")
    fun getAllOwners():LiveData<List<OwnerResponse.Data>>

}