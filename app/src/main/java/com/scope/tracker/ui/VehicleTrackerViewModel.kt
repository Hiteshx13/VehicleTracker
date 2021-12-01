package com.scope.tracker.ui

import android.text.Html
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.scope.tracker.network.Resource
import com.scope.tracker.ui.owner.OwnerResponse
import com.scope.tracker.ui.vehicle.VehicleResponse
import com.scope.tracker.util.Constants.Companion.GET_LOCATIONS
import com.scope.tracker.util.Constants.Companion.LIST
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response


class VehicleTrackerViewModel(private val ownerRepository: VehicleTrackerRepository) : ViewModel() {

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
        handleVehicleResponse(response)
    }

    fun updateOwnerVehicleData(ownerData: OwnerResponse.Data) = viewModelScope.launch {
        ownerRepository.upsert(ownerData)
    }

    /** handle vehicle data response from api*/

    private fun handleVehicleResponse(response: Response<ResponseBody>) {
        val data = response.body()?.string()
        Log.d("response:", "$data")

        if (response.body()?.contentType().toString().contains("json", true)) {
            val gson = Gson()
            if (data?.isNotEmpty() == true) {
                val model = gson.fromJson(data, VehicleResponse::class.java)
                if (response.isSuccessful) {
                    vehicleResponseAPI.postValue(Resource.Success(model))
                } else {
                    vehicleResponseAPI.postValue(Resource.Error(response.message()))
                }
            }
        } else {
            vehicleResponseAPI.postValue(
                Resource.Error(
                    Html.fromHtml(
                        data,
                        Html.FROM_HTML_MODE_COMPACT
                    ).toString().trim()
                )
            )
        }
    }
}