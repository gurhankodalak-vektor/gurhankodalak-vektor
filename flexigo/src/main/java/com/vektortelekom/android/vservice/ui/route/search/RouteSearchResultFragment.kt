package com.vektortelekom.android.vservice.ui.route.search

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.RouteSearchResultFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.route.adapter.RoutesDetailAdapter
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.utils.convertFullDateChangeDayAndMonth
import com.vektortelekom.android.vservice.utils.convertToShuttleDateTime
import com.vektortelekom.android.vservice.utils.convertToShuttleTime
import javax.inject.Inject

class RouteSearchResultFragment : BaseFragment<RouteSearchViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RouteSearchViewModel

    lateinit var binding: RouteSearchResultFragmentBinding

    private var routesDetailAdapter : RoutesDetailAdapter? = null
    private var tempValue : String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<RouteSearchResultFragmentBinding>(inflater, R.layout.route_search_result_fragment, container, false).apply {
            lifecycleOwner = this@RouteSearchResultFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavHostFragment.findNavController(this@RouteSearchResultFragment).navigateUp()
            }
        })

        val textToShow =  viewModel.fromLabelText.value
            .plus(" - ")
            .plus(viewModel.toLabelText.value)

        viewModel.campusAndLocationName.value = textToShow

        binding.textviewCampusName.text = textToShow
        tempValue = viewModel.dateValueText.value.toString()

        if (getString(R.string.generic_language) == "tr")
            binding.textviewDepartureTime.text = tempValue.convertFullDateChangeDayAndMonth().plus(", ").plus(viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext()))
        else
            binding.textviewDepartureTime.text = viewModel.selectedDate?.date.convertToShuttleTime(requireContext())


        binding.imageViewSort.setOnClickListener {
            viewModel.openNumberPicker.value = RouteSearchViewModel.SelectType.RouteSorting
        }

        binding.imageViewPreview.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_routesSearchResultFragment_to_routePreview)
        }

        binding.imageviewBack.setOnClickListener {
            viewModel.successNearbyRequest.value = false
            NavHostFragment.findNavController(this).navigateUp()
        }

        routesDetailAdapter = RoutesDetailAdapter { route ->
            viewModel.routeSelectedForReservation.value  = route
            viewModel.selectedStation = route.closestStation
            NavHostFragment.findNavController(this).navigate(R.id.action_routesSearchResultFragment_to_routeSearchReservation)
        }

        binding.recyclerViewBottomSheetRoutes.adapter = routesDetailAdapter

        viewModel.searchedRoutes.observe(viewLifecycleOwner) { routes ->
            if (routes != null) {
                viewModel.searchRoutesAdapterSetListTrigger.value = routes.toMutableList()
            }
        }

        viewModel.searchRoutesAdapterSetListTrigger.observe(viewLifecycleOwner) {
            if (it != null)
                if (it.size > 0)
                    routesDetailAdapter?.setList(it, "SHUTTLE")
        }

        viewModel.selectedRouteSortItemIndexTrigger.observe(viewLifecycleOwner) {
            if(it!= null) {
                viewModel.selectedRouteSortItemIndex = it

                viewModel.selectedRouteSortItemIndex?.let {  index ->

                    when(viewModel.routeSortList[index]) {
                        ShuttleViewModel.RouteSortType.WalkingDistance -> {
                            routesDetailAdapter?.getList()?.sortBy { route ->
                                route.closestStation?.durationInMin
                            }
                            routesDetailAdapter?.notifyDataSetChanged()
                        }
                        ShuttleViewModel.RouteSortType.TripDuration -> {
                            routesDetailAdapter?.getList()?.sortBy { route ->
                                route.durationInMin
                            }
                            routesDetailAdapter?.notifyDataSetChanged()
                        }
                        ShuttleViewModel.RouteSortType.OccupancyRatio -> {
                            routesDetailAdapter?.getList()?.sortBy { route ->
                                (100 * route.personnelCount) / route.vehicleCapacity
                            }
                            routesDetailAdapter?.notifyDataSetChanged()
                        }
                    }

                }
            }
        }

        binding.buttonNearbyStop.setOnClickListener {
            viewModel.createWorkgroupNearbyStationRequest()
        }

        binding.buttonCancel.setOnClickListener {
            viewModel.cancelWorkgroupNearbyStationRequest()
        }

        viewModel.successNearbyRequest.observe(viewLifecycleOwner) {
            if (it)
                successMessageDialog()
        }

        viewModel.successCancelNearbyRequest.observe(viewLifecycleOwner) {
            nearbyRequestLayoutVisibility()
        }

        viewModel.hasNearbyRequest.observe(viewLifecycleOwner) {
            binding.layoutButtons.visibility = View.VISIBLE
            if (it) {
                binding.textviewRequestNearby.visibility = View.GONE
                binding.buttonNearbyStop.visibility = View.GONE
                binding.buttonCancel.visibility = View.VISIBLE
            }
            else {
                if (viewModel.shouldShowRequestNearbyButton()) {
                    binding.textviewRequestNearby.visibility = View.VISIBLE
                    binding.buttonNearbyStop.visibility = View.VISIBLE
                    binding.buttonCancel.visibility = View.GONE
                }
                else {
                    binding.textviewRequestNearby.visibility = View.GONE
                    binding.buttonNearbyStop.visibility = View.GONE
                    binding.buttonCancel.visibility = View.GONE
                }
            }

        }

        viewModel.getWorkgroupNearbyStationRequest()
        binding.layoutButtons.visibility = View.GONE
    }

    private fun nearbyRequestLayoutVisibility() {
        if (viewModel.shouldShowRequestNearbyButton()) {
            binding.textviewRequestNearby.visibility = View.VISIBLE
            binding.buttonNearbyStop.visibility = View.VISIBLE
            binding.buttonCancel.visibility = View.GONE
        } else {
            binding.textviewRequestNearby.visibility = View.GONE
            binding.buttonNearbyStop.visibility = View.GONE
            binding.buttonCancel.visibility = View.GONE
        }
    }

    private fun successMessageDialog() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setCancelable(false)
        dialog.setMessage(resources.getString(R.string.requesting_nearby_success_message))
        dialog.setPositiveButton(resources.getString(R.string.Generic_Ok)) { d, _ ->
            d.dismiss()
            binding.textviewRequestNearby.visibility = View.GONE
            binding.buttonNearbyStop.visibility = View.GONE
            binding.buttonCancel.visibility = View.VISIBLE
        }
        dialog.show()
    }

    override fun getViewModel(): RouteSearchViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[RouteSearchViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "RouteSearchResultFragment"
        fun newInstance() = RouteSearchResultFragment()

    }

}