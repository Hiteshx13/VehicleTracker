package com.scope.tracker.ui.owner

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.scope.tracker.R
import com.scope.tracker.network.Resource
import com.scope.tracker.ui.VehicleTrackerViewModel
import com.scope.tracker.ui.VehicleTrackerActivity
import com.scope.tracker.util.AppUtils
import com.scope.tracker.util.AppUtils.getDeviceCurrentDate
import com.scope.tracker.util.AppUtils.getSavedDatefromPref
import com.scope.tracker.util.AppUtils.saveCurrentDateToPref
import com.scope.tracker.util.AppUtils.showToast
import kotlinx.android.synthetic.main.fragment_owner_list.*

class FragmentOwnerList : Fragment(R.layout.fragment_owner_list) {

    private lateinit var viewModel: VehicleTrackerViewModel
    private lateinit var ownerAdapter: OwnerListAdapter
    private val TAG = "OwnerListFragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as VehicleTrackerActivity).viewModel

        setupRecyclerView()
        setAdapterClickListener()
        setObserver()
        checkAndLoadOwnerData()
    }

    private fun setupRecyclerView() {
        ownerAdapter = OwnerListAdapter()
        rvOwners.apply {
            adapter = ownerAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    /**
     * Adapter click listener to navigate
     * on map screen for vehicles shown on map
     */
    private fun setAdapterClickListener() {
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


    /**
     * observers for API data and local storage data
     */
    private fun setObserver() {
        viewModel.getOwnerDataFromDB().observe(viewLifecycleOwner, { response ->
            ownerAdapter.differ.submitList(response)
            hideProgressBar()
        })

        /** observer for api data*/
        viewModel.ownerResponseAPI.observe(viewLifecycleOwner, { response ->
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
        })
    }

    /**
     * check if owner list fetched from api for today,
     * If data already fetched form server, than use it from
     * local storage*/
    private fun checkAndLoadOwnerData() {
        val savedDate = getSavedDatefromPref(requireContext())
        if (savedDate.isEmpty()) {
            callApiForOwnerData()
        } else {
            val deviceCurrentDate = getDeviceCurrentDate()
            if (savedDate.lowercase() == deviceCurrentDate.lowercase()) {
                viewModel.getOwnerDataFromDB()
            } else {
                callApiForOwnerData()
            }
        }
    }

    private fun callApiForOwnerData() {
        if (AppUtils.isInternetAvailable(requireContext())) {
            rvOwners.visibility = View.VISIBLE
            tvMessage.visibility = View.GONE
            viewModel.getOwnerDataFromAPI()

        } else {
            progressBar.visibility = View.GONE
            rvOwners.visibility = View.GONE
            tvMessage.visibility = View.VISIBLE

            val messageNoInternet = getString(R.string.please_check_device_internet_connection)
            tvMessage.text = messageNoInternet
            requireContext().showToast(messageNoInternet)
        }
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

}