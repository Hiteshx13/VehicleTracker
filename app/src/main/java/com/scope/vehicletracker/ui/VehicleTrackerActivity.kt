package com.scope.vehicletracker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.scope.vehicletracker.R
import com.scope.vehicletracker.db.OwnerDatabase
import com.scope.vehicletracker.ui.owner.OwnerRepository
import com.scope.vehicletracker.ui.owner.OwnerViewModel
import com.scope.vehicletracker.ui.owner.OwnerViewModelProviderFactory

class VehicleTrackerActivity : AppCompatActivity() {
    lateinit var viewModel: OwnerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ownerRepository = OwnerRepository(OwnerDatabase(this))
        val viewModelProviderFactory = OwnerViewModelProviderFactory(ownerRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[OwnerViewModel::class.java]


    }
}