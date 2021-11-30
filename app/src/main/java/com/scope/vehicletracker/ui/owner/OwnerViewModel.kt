package com.scope.vehicletracker.ui.owner

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.scope.vehicletracker.network.Resource
import com.scope.vehicletracker.network.response.owner.OwnerResponse
import com.scope.vehicletracker.network.response.vehicle.VehicleResponse
import com.scope.vehicletracker.util.Constants.Companion.GET_LOCATIONS
import com.scope.vehicletracker.util.Constants.Companion.LIST
import kotlinx.coroutines.launch
import retrofit2.Response
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class OwnerViewModel(private val ownerRepository: OwnerRepository) : ViewModel() {

    val ownerResponseAPI: MutableLiveData<Resource<OwnerResponse>> = MutableLiveData()
    val vehicleResponseAPI: MutableLiveData<Resource<VehicleResponse>> = MutableLiveData()


    fun getOwnerDataFromAPI() = viewModelScope.launch {
        ownerResponseAPI.postValue(Resource.Loading())
        val response = ownerRepository.getOwnerDataFromAPI(LIST)
        ownerResponseAPI.postValue(handleOwnerResponse(response))
    }

    /** handle owner data response from api*/
    private fun handleOwnerResponse(response: Response<OwnerResponse>): Resource<OwnerResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    fun saveOwnerData(ownerData: OwnerResponse.Data) = viewModelScope.launch {
        ownerRepository.upsert(ownerData)
    }

    fun getOwnerDataFromDB() = ownerRepository.getOwnerDataFromDB()


    fun deleteAllRecords() = viewModelScope.launch {
        ownerRepository.deleteAllRecords()
    }

    fun getVehicleDataFromAPI(userID: String) = viewModelScope.launch {
        vehicleResponseAPI.postValue(Resource.Loading())
        val response = ownerRepository.getVehicleDataFromAPI(GET_LOCATIONS, userID)


        Log.d("RESP:","${response.message()}")
        Log.d("RESP:","$response")

        if (response.body().toString().contains("<html>", true)) {
            vehicleResponseAPI.postValue(Resource.Error(response.message()))
        } else {

//            response.body().toString().contains("<html>", true)
            val gson=Gson()
            val strJson=gson.toJson(response.body())
            val model=gson.fromJson(strJson,VehicleResponse::class.java)

            if(response.isSuccessful){
                vehicleResponseAPI.postValue(Resource.Success(model))
            }else{
                vehicleResponseAPI.postValue(Resource.Error(response.message()))
            }
        }
    }

    fun updateOwnerVehicleData(ownerData: OwnerResponse.Data) = viewModelScope.launch {
        ownerRepository.upsert(ownerData)
    }

    /** handle vehicle data response from api*/
//<html><head><title>Server overloaded</title></head><body><h1>Server overloaded</h1></body></html>

    private fun handleVehicleResponse(response: Response<VehicleResponse>): Resource<VehicleResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }




}