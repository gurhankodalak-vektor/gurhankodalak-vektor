package com.vektortelekom.android.vservice.ui.route

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.RouteStopRequest
import com.vektortelekom.android.vservice.data.model.SearchRequestModel
import com.vektortelekom.android.vservice.data.model.StationModel
import com.vektortelekom.android.vservice.data.model.WorkgroupDirection
import com.vektortelekom.android.vservice.databinding.RouteSelectionFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.home.HomeActivity
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.shuttle.adapter.ShuttleRoutesAdapter
import com.vektortelekom.android.vservice.utils.convertHourMinutes
import com.vektortelekom.android.vservice.utils.convertToShuttleDateTime
import javax.inject.Inject

class RouteSelectionFragment : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: RouteSelectionFragmentBinding

    private var searchRoutesAdapter: ShuttleRoutesAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<RouteSelectionFragmentBinding>(inflater, R.layout.route_selection_fragment, container, false).apply {
            lifecycleOwner = this@RouteSelectionFragment
            viewModel = this@RouteSelectionFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getStops()

        viewModel.getWorkgroupInformation()

        viewModel.getWorkgroupNearbyStationRequest()

        binding.buttonSkip.setOnClickListener {
            showHomeActivity()
        }

        binding.imageViewPreview.setOnClickListener {
            viewModel.openBottomSheetRoutePreview.value = true
        }

        binding.imageViewSort.setOnClickListener {
            viewModel.openNumberPicker.value = ShuttleViewModel.SelectType.RouteSorting
        }

        binding.buttonCancel.setOnClickListener {
            viewModel.cancelWorkgroupNearbyStationRequest()
        }

        binding.buttonNearbyStop.setOnClickListener {
            viewModel.createWorkgroupNearbyStationRequest()
        }

        binding.imageviewRoutesBack.setOnClickListener {
            showHomeActivity()
        }

        searchRoutesAdapter = ShuttleRoutesAdapter { route ->

            var stop: StationModel? = route.closestStation

            if (route.closestStation?.name == null) {
                val isFirstLeg = viewModel.routeForWorkgroup.value!!.template.direction?.let { viewModel.isFirstLeg(it, viewModel.routeForWorkgroup.value!!.template.fromType!!) } == true
                stop = isFirstLeg.let { route.getRoutePath(it) }!!.stations.first()
            }

            viewModel.selectedRoute = route
            stop?.let {
                viewModel.selectedStopForReservation = stop
                val minuteText = getString(R.string.short_minute)
                val walkingDurationInMin = route.closestStation?.durationInMin?.toInt() ?: 0
                val walkingDurationInMinDisplayString = walkingDurationInMin.toString().plus(minuteText)

                viewModel.textViewBottomSheetStopName.value = stop.title

                viewModel.textViewBottomSheetRoutesTitle.value = route.title
                viewModel.textviewFullnessValue.value = "${route.personnelCount}/${route.vehicleCapacity}"
                viewModel.textViewDurationWalking.value = walkingDurationInMinDisplayString

                viewModel.isReturningShuttleEdit = true
                viewModel.isMakeReservationOpening = true

                viewModel.openBottomSheetMakeReservation.value = true

                viewModel.selectedRoute?.let {
                    viewModel.selectedStation = stop
                    viewModel.zoomStation = true
                    viewModel.openStopSelection.value = true
                }
            }
        }

        binding.recyclerViewBottomSheetRoutes.adapter = searchRoutesAdapter

        viewModel.searchedRoutes.observe(viewLifecycleOwner) { routes ->
            if (routes != null) {
                viewModel.searchRoutesAdapterSetListTrigger.value = routes.toMutableList()
                checkNearbyStation()
            }
        }
        viewModel.successNearbyRequest.observe(viewLifecycleOwner) {
            if (it != null && it == true) {
                successMessageDialog()
            }
        }

        viewModel.routeForWorkgroup.observe(viewLifecycleOwner) {
            if (it?.template != null) {
                viewModel.workgroupType.value = it.template.workgroupType
                viewModel.textViewBottomSheetRoutesFromToName.value = it.template.name

                if (it.template.direction == WorkgroupDirection.ROUND_TRIP) {
                    val firstDeparture = it.template.shift?.departureHour.convertHourMinutes(requireContext())
                            ?: it.template.shift?.arrivalHour.convertHourMinutes(requireContext())
                    val returnDeparture = it.template.shift?.returnDepartureHour.convertHourMinutes(requireContext())
                            ?: it.template.shift?.returnArrivalHour.convertHourMinutes(requireContext())
                    binding.textviewArrivalTime.text = getString(R.string.departure, firstDeparture).plus(", ").plus(getString(R.string.arrival, returnDeparture))
                } else {
                    val firstDeparture = it.template.shift?.departureHour.convertHourMinutes(requireContext())
                            ?: it.template.shift?.arrivalHour.convertHourMinutes(requireContext())
                    if (firstDeparture != null) {
                        binding.textviewArrivalTime.text = getString(R.string.departure, firstDeparture)
                    } else
                        binding.textviewArrivalTime.text = getString(R.string.departure, it.instance.firstDepartureDate.convertToShuttleDateTime(requireContext()))
                }

            }
        }

        viewModel.searchRoutesAdapterSetMyDestinationTrigger.observe(viewLifecycleOwner) {
            if (it != null)
                searchRoutesAdapter?.setMyDestination(it)
        }

        viewModel.selectedRouteSortItemIndexTrigger.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.selectedRouteSortItemIndex = it

                viewModel.selectedRouteSortItemIndex?.let { index ->

                    when (viewModel.routeSortList[index]) {
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
            if (it != null)
                if (it.size > 0)
                    searchRoutesAdapter?.setList(it, viewModel.workgroupType.value)
                else
                    showHomeActivity()
        }

    }

    private fun successMessageDialog() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setCancelable(false)
        dialog.setMessage(resources.getString(R.string.requesting_nearby_success_message))
        dialog.setPositiveButton(resources.getString(R.string.Generic_Ok)) { d, _ ->
            d.dismiss()
            showHomeActivity()
        }
        dialog.show()
    }

    private fun buttonSecondaryStyle(button: MaterialButton){
        button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(button.context, R.color.colorPrimary))
        button.setTextColor(ContextCompat.getColor(button.context, R.color.mdtp_white))
        button.setBackgroundResource(R.drawable.button_continue)
    }

    private fun checkNearbyStation() {
        val duration = viewModel.searchedRoutes.value?.first()?.durationInMin
        val shortParameter = AppDataManager.instance.mobileParameters.shortNearbyStationDurationInMin
        val longParameter = AppDataManager.instance.mobileParameters.longNearbyStationDurationInMin

        if (duration != null) {

            if (duration > longParameter) {
                binding.layoutDontHaveRoute.visibility = View.VISIBLE
                binding.layoutHaveRoutes.visibility = View.GONE
                binding.imageViewSort.visibility = View.GONE
                binding.imageViewPreview.visibility = View.GONE
                binding.buttonNearbyStop.visibility = View.VISIBLE
                binding.textviewStopLocation.visibility = View.VISIBLE

                if (viewModel.isFromAddressSelect) {
                    binding.buttonSkip.visibility = View.VISIBLE
                    buttonSecondaryStyle(binding.buttonNearbyStop)
                }
                if (viewModel.hasNearbyRequest.value == true){
                    binding.textviewStopLocation.text = getString(R.string.nearby_request_processed)
                    binding.buttonNearbyStop.visibility = View.GONE
                    binding.buttonCancel.visibility = View.VISIBLE
                }

            } else if (duration > shortParameter) {

                binding.buttonNearbyStop.visibility = View.VISIBLE
                binding.layoutDontHaveRoute.visibility = View.GONE
                binding.layoutHaveRoutes.visibility = View.VISIBLE
                binding.imageViewSort.visibility = View.VISIBLE
                binding.imageViewPreview.visibility = View.VISIBLE

                binding.textviewStopLocation.visibility = View.GONE

                if (viewModel.isFromAddressSelect) {
                    binding.buttonSkip.visibility = View.VISIBLE
                    buttonSecondaryStyle(binding.buttonNearbyStop)
                }
                if (viewModel.hasNearbyRequest.value == true){
                    binding.textviewStopLocation.visibility = View.GONE
                    binding.textviewStopLocation.text = getString(R.string.nearby_request_processed)
                    binding.buttonNearbyStop.visibility = View.GONE
                    binding.buttonCancel.visibility = View.VISIBLE
                }

            } else {

                binding.buttonNearbyStop.visibility = View.GONE
                binding.layoutDontHaveRoute.visibility = View.GONE
                binding.textviewStopLocation.visibility = View.GONE
                binding.layoutHaveRoutes.visibility = View.VISIBLE

                if (viewModel.isFromAddressSelect) {
                    binding.buttonSkip.visibility = View.VISIBLE
                }
                if (viewModel.hasNearbyRequest.value == true){
                    binding.textviewStopLocation.text = getString(R.string.nearby_request_processed)
                    binding.buttonCancel.visibility = View.VISIBLE
                    binding.textviewStopLocation.visibility = View.VISIBLE
                    binding.buttonNearbyStop.visibility = View.VISIBLE
                } else{
                    binding.buttonNearbyStop.visibility = View.GONE
                }
            }
        }
    }

    private fun getStops() {
        val from = SearchRequestModel(
                lat = null,
                lng = null,
                destinationId = AppDataManager.instance.personnelInfo?.destination?.id
        )


        val to = SearchRequestModel(
                lat = AppDataManager.instance.personnelInfo?.homeLocation?.latitude,
                lng = AppDataManager.instance.personnelInfo?.homeLocation?.longitude,
                destinationId = null
        )

        viewModel.getStops(
                RouteStopRequest(
                        from = from,
                        whereto = to,
                        shiftId = null,
                        workgroupInstanceId = AppDataManager.instance.personnelInfo?.workgroupInstanceId ?: 0
                ),
                true,
                requireContext()
        )
    }

    private fun showHomeActivity() {
        viewModel.isFromAddressSelect = false
        viewModel.isComingSurvey = false
        viewModel.successNearbyRequest.value = false
        activity?.finish()
        val intent = Intent(requireContext(), HomeActivity::class.java)
        startActivity(intent)
    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "RouteSelectionFragment"
        fun newInstance() = RouteSelectionFragment()

    }

}