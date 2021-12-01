package com.scope.tracker.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scope.tracker.ui.VehicleTrackerRepository
import com.scope.tracker.ui.VehicleTrackerViewModel

class OwnerViewModelProviderFactory(
    private val ownerRepository: VehicleTrackerRepository
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VehicleTrackerViewModel(ownerRepository) as T
    }
}