package com.scope.vehicletracker.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OwnerViewModelProviderFactory(
    private val ownerRepository: OwnerRepository
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OwnerViewModel(ownerRepository) as T
    }
}