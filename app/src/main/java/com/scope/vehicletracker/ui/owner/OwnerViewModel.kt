package com.scope.vehicletracker.ui.owner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scope.vehicletracker.network.Resource
import com.scope.vehicletracker.network.response.owner.OwnerResponse
import com.scope.vehicletracker.network.response.vehicle.VehicleResponse
import com.scope.vehicletracker.util.Constants.Companion.GET_LOCATIONS
import com.scope.vehicletracker.util.Constants.Companion.LIST
import kotlinx.coroutines.launch
import retrofit2.Response

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

    /** handle vehicle data response from api*/
    private fun handleVehicleResponse(response: Response<VehicleResponse>): Resource<VehicleResponse> {
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
        vehicleResponseAPI.postValue(handleVehicleResponse(response))
    }

    fun updateOwnerVehicleData(ownerData: OwnerResponse.Data) = viewModelScope.launch {
        ownerRepository.upsert(ownerData)
    }
}