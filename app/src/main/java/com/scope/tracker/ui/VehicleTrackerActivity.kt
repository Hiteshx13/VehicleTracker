package com.scope.tracker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.scope.tracker.R
import com.scope.tracker.db.OwnerDatabase
import com.scope.tracker.ui.owner.OwnerViewModelProviderFactory

class VehicleTrackerActivity : AppCompatActivity() {
    lateinit var viewModel: VehicleTrackerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ownerRepository = VehicleTrackerRepository(OwnerDatabase(this))
        val viewModelProviderFactory = OwnerViewModelProviderFactory(ownerRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[VehicleTrackerViewModel::class.java]

    }
}