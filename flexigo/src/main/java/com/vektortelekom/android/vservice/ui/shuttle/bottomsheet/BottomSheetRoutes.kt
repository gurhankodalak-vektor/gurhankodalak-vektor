package com.vektortelekom.android.vservice.ui.shuttle.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.WorkgroupDirection
import com.vektortelekom.android.vservice.databinding.BottomSheetRoutesBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.shuttle.adapter.ShuttleRoutesAdapter
import com.vektortelekom.android.vservice.utils.convertHourMinutes
import com.vektortelekom.android.vservice.utils.convertToShuttleDateTime
import javax.inject.Inject

class BottomSheetRoutes : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: BottomSheetRoutesBinding

    private var searchRoutesAdapter : ShuttleRoutesAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<BottomSheetRoutesBinding>(inflater, R.layout.bottom_sheet_routes, container, false).apply {
            lifecycleOwner = this@BottomSheetRoutes
            viewModel = this@BottomSheetRoutes.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageViewSort.setOnClickListener {
            viewModel.openNumberPicker.value = ShuttleViewModel.SelectType.RouteSorting
        }

        binding.imageViewPreview.setOnClickListener {
            viewModel.openBottomSheetRoutePreview.value = true
        }

        binding.imageViewBottomSheetRoutesBack.setOnClickListener {
            viewModel.isReturningShuttleEdit = true
            childFragmentManager.popBackStack()
            if (viewModel.isShuttleServicePlaningFragment){
                viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_HIDDEN
                viewModel.bottomSheetVisibility.value = false
            } else{
                viewModel.openBottomSheetEditShuttle.value = true
            }
        }

        searchRoutesAdapter = ShuttleRoutesAdapter { route ->
            viewModel.routeSelectedForReservation.value = route
        }

        binding.recyclerViewBottomSheetRoutes.adapter = searchRoutesAdapter

        viewModel.searchRoutesAdapterSetMyDestinationTrigger.observe(viewLifecycleOwner) {
            if(it != null)
                searchRoutesAdapter?.setMyDestination(it)
        }

        viewModel.selectedRouteSortItemIndexTrigger.observe(viewLifecycleOwner) {
            if(it!= null) {
                viewModel.selectedRouteSortItemIndex = it

                viewModel.selectedRouteSortItemIndex?.let {  index ->

                    when(viewModel.routeSortList[index]) {
                        ShuttleViewModel.RouteSortType.WalkingDistance -> {
                            searchRoutesAdapter?.getList()?.sortBy { route ->
                                route.closestStation?.durationInMin
                            }
                            searchRoutesAdapter?.notifyDataSetChanged()
                        }
                        ShuttleViewModel.RouteSortType.TripDuration -> {
                            searchRoutesAdapter?.getList()?.sortBy { route ->
                                route.durationInMin
                            }
                            searchRoutesAdapter?.notifyDataSetChanged()
                        }

                        ShuttleViewModel.RouteSortType.OccupancyRatio -> {
                            searchRoutesAdapter?.getList()?.sortBy { route ->
                                (100 * route.personnelCount) / route.vehicleCapacity
                            }
                            searchRoutesAdapter?.notifyDataSetChanged()
                        }
                    }

                }
            }
        }

        viewModel.searchRoutesAdapterSetListTrigger.observe(viewLifecycleOwner) {
            if(it!= null) {
                if (viewModel.workgroupTemplate != null) {

                    if (viewModel.workgroupTemplate!!.direction == WorkgroupDirection.ROUND_TRIP) {
                        val firstDeparture = viewModel.workgroupTemplate!!.shift?.departureHour.convertHourMinutes(requireContext())
                            ?: viewModel.workgroupTemplate!!.shift?.arrivalHour.convertHourMinutes(requireContext())
                        val returnDeparture = viewModel.workgroupTemplate!!.shift?.returnDepartureHour.convertHourMinutes(requireContext())
                            ?: viewModel.workgroupTemplate!!.shift?.returnArrivalHour.convertHourMinutes(requireContext())

                        binding.textviewArrivalTime.text = getString(R.string.departure, firstDeparture).plus(", ").plus(getString(R.string.arrival, returnDeparture))
                        viewModel.arrivalDepartureTime = firstDeparture.plus(" - ").plus(returnDeparture)

                    } else {
                        val firstDeparture = viewModel.workgroupTemplate!!.shift?.departureHour.convertHourMinutes(requireContext())
                            ?: viewModel.workgroupTemplate!!.shift?.arrivalHour.convertHourMinutes(requireContext())
                        if (firstDeparture != null) {
                            binding.textviewArrivalTime.text = getString(R.string.departure, firstDeparture)
                            viewModel.arrivalDepartureTime = firstDeparture

                        } else {
                            binding.textviewArrivalTime.text = getString(R.string.departure, viewModel.workgroupInstance!!.firstDepartureDate.convertToShuttleDateTime(requireContext()))
                            viewModel.arrivalDepartureTime = viewModel.workgroupInstance!!.firstDepartureDate.convertToShuttleDateTime(requireContext())

                        }
                    }

                }

                searchRoutesAdapter?.setList(it, viewModel.workgroupType.value)
            }
        }

    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "BottomSheetRoutes"
        fun newInstance() = BottomSheetRoutes()

    }

}