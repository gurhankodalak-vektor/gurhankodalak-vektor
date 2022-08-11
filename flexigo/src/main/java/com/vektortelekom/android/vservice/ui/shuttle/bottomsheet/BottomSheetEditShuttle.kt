package com.vektortelekom.android.vservice.ui.shuttle.bottomsheet

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.BottomSheetEditShuttleBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.utils.convertToShuttleDateTime
import com.vektortelekom.android.vservice.utils.fromHtml
import javax.inject.Inject

class BottomSheetEditShuttle : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: BottomSheetEditShuttleBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<BottomSheetEditShuttleBinding>(inflater, R.layout.bottom_sheet_edit_shuttle, container, false).apply {
            lifecycleOwner = this@BottomSheetEditShuttle
            viewModel = this@BottomSheetEditShuttle.viewModel
        }

        return binding.root
    }
    var from: SearchRequestModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.currentRide?.routeId == null) {
            binding.switchBottomSheetEditShuttleUse.visibility = View.GONE
            binding.buttonBottomSheetEditShuttleRouteSubmit.text = getString(R.string.search_shuttle)
        }
        else{
            binding.switchBottomSheetEditShuttleUse.visibility = View.VISIBLE
            binding.buttonBottomSheetEditShuttleRouteSubmit.text = getString(R.string.save)
        }
        // TODO: multihourlarda editlenemez alanlar var. multihour da false bu alan
        if (viewModel.isMultipleHours){
            if (viewModel.isFromCampus){
                binding.imageViewBottomSheetEditShuttleRouteFrom.visibility = View.GONE
                binding.imageViewBottomSheetEditShuttleRouteTo.visibility = View.VISIBLE
            } else
            {
                binding.imageViewBottomSheetEditShuttleRouteFrom.visibility = View.VISIBLE
                binding.imageViewBottomSheetEditShuttleRouteTo.visibility = View.GONE
            }
        } else
        {
            binding.imageViewBottomSheetEditShuttleRouteFrom.visibility = View.VISIBLE
            binding.imageViewBottomSheetEditShuttleRouteTo.visibility = View.VISIBLE
        }

        binding.switchBottomSheetEditShuttleUse.setOnCheckedChangeListener { buttonView, _ ->

            if(buttonView.isPressed) {
                var title = getString(R.string.wont_use_shuttle_2)

                if (viewModel.currentRide?.notUsing == true) {
                    title = getString(R.string.use_shuttle)
                }

                val dialog = AlertDialog.Builder(requireContext())
                dialog.setCancelable(false)
                dialog.setTitle(fromHtml("<b>$title</b>"))
                dialog.setMessage(resources.getString(R.string.not_attending_selection_message))
                dialog.setPositiveButton(resources.getString(R.string.selected_date)) { d, _ ->
                    d.dismiss()
                    viewModel.currentRide?.let { viewModel.changeShuttleSelectedDate(it, null, null, true) }
                }
                dialog.setNegativeButton(resources.getString(R.string.multiple_dates)) { d, _ ->
                    d.dismiss()
                    viewModel.calendarSelectedRides.value = viewModel.currentRide
                    viewModel.openBottomSheetCalendar.value = true

                }
                dialog.setNeutralButton(resources.getString(R.string.cancel)) { d, _ ->
                    d.dismiss()
                    binding.switchBottomSheetEditShuttleUse.isChecked = false
                }

                dialog.show()

              /*
                FlexigoInfoDialog.Builder(requireContext())
                        .setTitle(getString(R.string.wont_use_shuttle_2))
                        .setText1(getString(R.string.shuttle_cancel_text, viewModel.currentRide?.firstDepartureDate.convertToShuttleReservationTime2(), viewModel.currentRide?.routeName ?: ""))
                        .setCancelable(false)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.confirm)) { dialog ->
                            dialog.dismiss()
                            val firstLegUsage = if (viewModel.currentRide?.firstLeg == true) (viewModel.currentRide?.notUsing == true) else null
                            val returnLegUsage = if (viewModel.currentRide?.firstLeg == false) (viewModel.currentRide?.notUsing == true) else null

                            viewModel.makeShuttleReservation2(request = ShuttleReservationRequest2(
                                    reservationDay = Date(viewModel.currentRide?.firstDepartureDate ?: 0L).convertForBackend2(),
                                    reservationDayEnd = null,
                                    workgroupInstanceId = viewModel.currentRide?.workgroupInstanceId ?: 0,
                                    routeId = viewModel.currentRide?.routeId ?: 0L,
                                    useFirstLeg = firstLegUsage,
                                    firstLegStationId = null,
                                    useReturnLeg = returnLegUsage,
                                    returnLegStationId = null
                            ), isVisibleMessage = false)
                        }
                        .setCancelButton(getString(R.string.cancel_2)) { dialog ->
                            dialog.dismiss()
                            binding.switchBottomSheetEditShuttleUse.isChecked = false
                        }
                        .create()
                        .show()*/
            }

        }

        binding.buttonBottomSheetEditShuttleRouteSubmit.setOnClickListener {
            viewModel.selectedDate?.let { selectedDate ->

                when(selectedDate.workgroupStatus) {
                    WorkgroupStatus.PENDING_DEMAND -> {
                        FlexigoInfoDialog.Builder(requireContext())
                                .setTitle(getString(R.string.shuttle_request))
                                .setText1(String.format(getString(R.string.shuttle_demand_info), viewModel.selectedToLocation?.text, selectedDate.date.convertToShuttleDateTime()))
                                .setCancelable(false)
                                .setIconVisibility(false)
                                .setOkButton(getString(R.string.shuttle_send_demand)) { dialog ->
                                    dialog.dismiss()

                                    var selectedStation : DestinationModel? = null
                                    var selectedLocation: ShuttleViewModel.FromToLocation? = null

                                    if(viewModel.isFromChanged) {
                                        selectedStation = viewModel.selectedFromDestination
                                        selectedLocation = viewModel.selectedFromLocation
                                    }

                                    if(viewModel.isToChanged) {
                                        if(selectedStation == null) {
                                            selectedStation = viewModel.selectedToDestination
                                        }
                                        if(selectedLocation == null) {
                                            selectedLocation = viewModel.selectedToLocation
                                        }

                                    }

                                    viewModel.demandWorkgroup(WorkgroupDemandRequest(
                                            workgroupInstanceId = selectedDate.workgroupId,
                                            stationId = viewModel.selectedFromDestination?.id?:viewModel.selectedToDestination?.id,
                                            location = if(selectedLocation == null) {
                                                val homeLocationModel = AppDataManager.instance.personnelInfo?.homeLocation
                                                LocationModel2(latitude = homeLocationModel?.latitude, longitude = homeLocationModel?.longitude)
                                            }
                                            else {
                                                LocationModel2(latitude = selectedLocation.location.latitude, longitude = selectedLocation.location.longitude)
                                            }
                                    ))

                                }
                                .setCancelButton(getString(R.string.Generic_Close)) { dialog ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                    }
                    else -> {
                        if (viewModel.isShuttleServicePlaningFragment){

                            val from: SearchRequestModel
                            val to: SearchRequestModel
                            if (viewModel.workgroupTemplate?.fromType == FromToType.CAMPUS || viewModel.workgroupTemplate?.fromType == FromToType.PERSONNEL_WORK_LOCATION) {
                                to = SearchRequestModel(
                                        lat = AppDataManager.instance.personnelInfo?.homeLocation?.latitude,
                                        lng = AppDataManager.instance.personnelInfo?.homeLocation?.longitude,
                                        destinationId = null
                                )
                                from = SearchRequestModel(
                                        lat = null,
                                        lng = null,
                                        destinationId = viewModel.workgroupTemplate?.fromTerminalReferenceId
                                )
                            }
                            else {
                                from = SearchRequestModel(
                                        lat = AppDataManager.instance.personnelInfo?.homeLocation?.latitude,
                                        lng = AppDataManager.instance.personnelInfo?.homeLocation?.longitude,
                                        destinationId = null
                                )
                                to = SearchRequestModel(
                                        lat = null,
                                        lng = null,
                                        destinationId = viewModel.workgroupTemplate?.toTerminalReferenceId
                                )
                            }

                            viewModel.getStops(
                                    RouteStopRequest(
                                            from = from,
                                            whereto = to,
                                            shiftId = null,
                                            workgroupInstanceId = selectedDate.workgroupId
                                    ),
                                    true,
                                    requireContext()
                            )

//
//                             from = SearchRequestModel(
//                                    lat = null,
//                                    lng = null,
//                                    destinationId = AppDataManager.instance.personnelInfo?.destination?.id
//                            )
//                            val to = if (viewModel.selectedFromDestination != null)
//                                SearchRequestModel(
//                                        lat = viewModel.selectedToLocation?.location?.latitude,
//                                        lng = viewModel.selectedToLocation?.location?.longitude,
//                                        destinationId = null
//                                )
//                            else
//                                SearchRequestModel(
//                                        lat = null,
//                                        lng = null,
//                                        destinationId = viewModel.selectedToDestination!!.id
//                                )
//
//                            viewModel.getStops(
//                                    RouteStopRequest(
//                                            from = from!!,
//                                            whereto = to,
//                                            shiftId = null,
//                                            workgroupInstanceId = selectedDate.workgroupId
//                                    ),
//                                    true,
//                                    requireContext()
//                            )
                        } else{
                             from = if (viewModel.selectedFromDestination != null)
                                SearchRequestModel(
                                        lat = null,
                                        lng = null,
                                        destinationId = viewModel.selectedFromDestination!!.id
                                )
                            else
                                SearchRequestModel(
                                        lat = viewModel.selectedFromLocation?.location?.latitude,
                                        lng = viewModel.selectedFromLocation?.location?.longitude,
                                        destinationId = null
                                )

                            val to = if (viewModel.selectedFromDestination != null)
                                SearchRequestModel(
                                        lat = viewModel.selectedToLocation?.location?.latitude,
                                        lng = viewModel.selectedToLocation?.location?.longitude,
                                        destinationId = null
                                )
                            else
                                SearchRequestModel(
                                        lat = null,
                                        lng = null,
                                        destinationId = viewModel.selectedToDestination!!.id
                                )

                            viewModel.getStops(
                                    RouteStopRequest(
                                            from = from!!,
                                            whereto = to,
                                            shiftId = null,
                                            workgroupInstanceId = selectedDate.workgroupId
                                    )
                            )

                        }

                    }
                }

            }

        }

        binding.layoutBottomSheetEditShuttleRouteFrom.setOnClickListener {

          //  if(!viewModel.isShuttleServicePlaningFragment) {
                viewModel.autocompletePredictions.value = null
                viewModel.setSearchListAdapter.value = true
                viewModel.editTextAddressSearch.value = ""


                if (viewModel.currentRide?.fromTerminalReferenceId == null) {
                    viewModel.isFromToShown = true
                    viewModel.isFrom = true
                    viewModel.openBottomSheetFromWhere.value = true
                    viewModel.imageViewFromToRes.value = R.drawable.ic_from
                    viewModel.textViewFromToTextColor.value = ContextCompat.getColor(requireContext(), R.color.purpley)
                    viewModel.textViewFromToText.value = getString(R.string.from)
                    viewModel.toLocation = SearchRequestModel(
                            lat = viewModel.myRouteDetails.value?.destination?.location?.latitude ?: 0.0,
                            lng = viewModel.myRouteDetails.value?.destination?.location?.longitude ?: 0.0,
                            destinationId = viewModel.myRouteDetails.value?.destination?.id
                    )
                } else {

                    viewModel.openNumberPicker.value = ShuttleViewModel.SelectType.CampusFrom
                }

         //   }
        }

        binding.layoutBottomSheetEditShuttleRouteTo.setOnClickListener {
            viewModel.autocompletePredictions.value = null
            viewModel.setSearchListAdapter.value = true
            viewModel.editTextAddressSearch.value = ""

            if (viewModel.isShuttleServicePlaningFragment){
                if (viewModel.workgroupTemplate?.toTerminalReferenceId == null){
                    viewModel.isFromToShown = true
                    viewModel.isFrom = false
                    viewModel.openBottomSheetFromWhere.value = true
                    viewModel.imageViewFromToRes.value = R.drawable.ic_to
                    viewModel.textViewFromToTextColor.value = ContextCompat.getColor(requireContext(), R.color.marigold)
                    viewModel.textViewFromToText.value = getString(R.string.to)

                    viewModel.fromLocation = SearchRequestModel(
                            lat = viewModel.myRouteDetails.value?.destination?.location?.latitude?:0.0,
                            lng = viewModel.myRouteDetails.value?.destination?.location?.longitude?:0.0,
                            destinationId = viewModel.myRouteDetails.value?.destination?.id
                    )
                } else
                {
                    viewModel.openNumberPicker.value = ShuttleViewModel.SelectType.CampusTo
                }
            } else{
                if(viewModel.currentRide?.toTerminalReferenceId == null) {
                    viewModel.isFromToShown = true
                    viewModel.isFrom = false
                    viewModel.openBottomSheetFromWhere.value = true
                    viewModel.imageViewFromToRes.value = R.drawable.ic_to
                    viewModel.textViewFromToTextColor.value = ContextCompat.getColor(requireContext(), R.color.marigold)
                    viewModel.textViewFromToText.value = getString(R.string.to)
                    viewModel.fromLocation = SearchRequestModel(
                            lat = viewModel.myRouteDetails.value?.destination?.location?.latitude?:0.0,
                            lng = viewModel.myRouteDetails.value?.destination?.location?.longitude?:0.0,
                            destinationId = viewModel.myRouteDetails.value?.destination?.id
                    )

                }
                else {
                    viewModel.openNumberPicker.value = ShuttleViewModel.SelectType.CampusTo

                }
            }

        }

        binding.layoutBottomSheetEditShuttleRouteTime.setOnClickListener {
            viewModel.dateAndWorkgroupList?.let {

                viewModel.openNumberPicker.value = ShuttleViewModel.SelectType.Time

            }
        }

        binding.layoutBottomSheetEditShuttleRouteSearch.setOnClickListener {
            viewModel.openBottomSheetSearchRoute.value = true
        }

    }


    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "BottomSheetEditShuttle"

        fun newInstance() = BottomSheetEditShuttle()

    }

}