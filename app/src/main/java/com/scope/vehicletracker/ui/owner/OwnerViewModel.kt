package com.scope.vehicletracker.ui.owner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scope.vehicletracker.network.Resource
import com.scope.vehicletracker.network.response.owner.OwnerResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class OwnerViewModel(val ownerRepository: OwnerRepository) : ViewModel() {

    val ownerResponse: MutableLiveData<Resource<OwnerResponse>> = MutableLiveData()

    init {
        getOwnerList("list")
    }

    private fun getOwnerList(op: String) = viewModelScope.launch {
        ownerResponse.postValue(Resource.Loading())
        val response = ownerRepository.getOwnerList(op)
        ownerResponse.postValue(handleOwnerResponse(response))
    }

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

    fun getAllOwners() = ownerRepository.getOwnersData()
}