package com.scope.vehicletracker.ui.owner

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.scope.vehicletracker.R
import com.scope.vehicletracker.network.Resource
import com.scope.vehicletracker.network.response.owner.OwnerResponse
import com.scope.vehicletracker.ui.VehicleTrackerActivity
import com.scope.vehicletracker.util.AppUtils.getDeviceCurrentDate
import com.scope.vehicletracker.util.AppUtils.getSavedDatefromPref
import com.scope.vehicletracker.util.AppUtils.saveCurrentDateToPref
import kotlinx.android.synthetic.main.fragment_owner_list.*

class FragmentOwnerList : Fragment(R.layout.fragment_owner_list) {

    private lateinit var viewModel: OwnerViewModel
    private lateinit var ownerAdapter: OwnerListAdapter
    private val TAG = "OwnerListFragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as VehicleTrackerActivity).viewModel

        setupRecyclerView()
        setAdapterClickListener()
        setObserver()
        chechAndLoadOwnerData()
    }

    private fun setupRecyclerView() {
        ownerAdapter = OwnerListAdapter()
        rvOwners.apply {
            adapter = ownerAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun setAdapterClickListener(){
        ownerAdapter.setOnItemClickListener {

            val bundle = Bundle().apply {
                putSerializable("owner_data", it)
            }
            findNavController().navigate(
                R.id.action_fragmentUserList_to_fragmentMapUserVehicle,
                bundle
            )
        }
    }


    private fun setObserver() {
        viewModel.getOwnerDataFromDB().observe(viewLifecycleOwner, {response->
            ownerAdapter.differ.submitList(response)
            hideProgressBar()
        })

        viewModel.ownerResponseAPI.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    viewModel.deleteAllRecords()
                    response.data?.let { ownerResponse ->

                        saveCurrentDateToPref(requireContext())
                        ownerResponse.data?.forEach { response ->
                            if (response?.owner != null) { // Removing empty data from response
                                viewModel.saveOwnerData(response)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Log.d(TAG, "Error occured: $message ")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }

            }
//            viewModel.getOwnerDataFromDB()
        })
    }

    private fun chechAndLoadOwnerData(){
        val savedDate = getSavedDatefromPref(requireContext())
        if (savedDate.isEmpty()) {
            viewModel.getOwnerDataFromAPI()
        } else {
            val devicCurrentDate = getDeviceCurrentDate()
            if (savedDate.lowercase() == devicCurrentDate.lowercase()) {
                viewModel.getOwnerDataFromDB()
            } else {
                viewModel.getOwnerDataFromAPI()
            }
        }
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }


}