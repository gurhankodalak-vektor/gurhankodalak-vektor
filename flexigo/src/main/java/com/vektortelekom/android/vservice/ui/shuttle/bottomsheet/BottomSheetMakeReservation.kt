package com.vektortelekom.android.vservice.ui.shuttle.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.ShuttleReservationRequest2
import com.vektortelekom.android.vservice.databinding.BottomSheetMakeReservationBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.utils.convertForBackend2
import com.vektortelekom.android.vservice.utils.convertToShuttleReservationTime
import com.vektortelekom.android.vservice.utils.convertToShuttleReservationTime2
import java.util.*
import javax.inject.Inject

class BottomSheetMakeReservation : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: BottomSheetMakeReservationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<BottomSheetMakeReservationBinding>(inflater, R.layout.bottom_sheet_make_reservation, container, false).apply {
            lifecycleOwner = this@BottomSheetMakeReservation
            viewModel = this@BottomSheetMakeReservation.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageViewStopLocation.setOnClickListener {
            viewModel.navigateToMapTrigger.value = true
        }

        binding.buttonBottomSheetReservationReserve.setOnClickListener {

            if(viewModel.isShuttleServicePlaningFragment){
                viewModel.selectedDate?.let { selectedDate ->
                    viewModel.selectedStopForReservation?.let { stop ->

                        FlexigoInfoDialog.Builder(requireContext())
                            .setTitle(getString(R.string.reservation))
                            .setText1(getString(R.string.shuttle_make_reservation_info_text,
                                (stop.route?.route?.name ?: ""),
                                        selectedDate.date.convertToShuttleReservationTime2(requireContext()),""))
                            .setCancelable(false)
                            .setIconVisibility(false)
                            .setOkButton(getString(R.string.confirm)) { dialog ->
                                dialog.dismiss()
                                val firstLeg = selectedDate.ride.firstLeg

                                viewModel.makeShuttleReservation2(request = ShuttleReservationRequest2(
                                    reservationDay = Date(selectedDate.ride.firstDepartureDate).convertForBackend2(),
                                    reservationDayEnd = null,
                                    workgroupInstanceId = selectedDate.ride.workgroupInstanceId,
                                    routeId = stop.routeId,
                                    useFirstLeg = firstLeg,
                                    firstLegStationId = if (firstLeg) stop.id else null,
                                    useReturnLeg = if (firstLeg.not()) true else null,
                                    returnLegStationId = if (firstLeg.not()) stop.id else null
                                ), isVisibleMessage = true)
                            }
                            .setCancelButton(getString(R.string.cancel_2)) { dialog ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                }
            } else{
                viewModel.selectedStopForReservation?.let {  stop ->

                    FlexigoInfoDialog.Builder(requireContext())
                        .setTitle(getString(R.string.reservation))
                        .setText1(getString(R.string.shuttle_make_reservation_info_text,
                            (stop.route?.route?.name?:""),
                                    viewModel.selectedDate?.date.convertToShuttleReservationTime(requireContext()),""))
                        .setCancelable(false)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.confirm)) { dialog ->
                            dialog.dismiss()
                            val firstLeg = viewModel.selectedDate?.ride?.firstLeg?:false

                            viewModel.makeShuttleReservation2(request = ShuttleReservationRequest2(
                                reservationDay = Date(viewModel.selectedDate?.date?:0L).convertForBackend2(),
                                reservationDayEnd = null,
                                workgroupInstanceId = viewModel.selectedDate?.ride?.workgroupInstanceId?:0,
                                routeId = stop.routeId,
                                useFirstLeg = firstLeg,
                                firstLegStationId = if(firstLeg) stop.id else null,
                                useReturnLeg = if(firstLeg.not()) true else null,
                                returnLegStationId = if(firstLeg.not()) stop.id else null
                            ), isVisibleMessage = true)
                        }
                        .setCancelButton(getString(R.string.cancel_2)) { dialog ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()

                }
            }
        }

        binding.buttonBottomSheetReservationUsual.setOnClickListener {

            FlexigoInfoDialog.Builder(requireContext())
                    .setTitle(getString(R.string.shuttle_change_info_title))
                    .setText1(getString(R.string.shuttle_route_change_info_text))
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.confirm_change)) { dialog ->
                        dialog.dismiss()
                        viewModel.selectedStation?.let {
                            viewModel.updatePersonnelStation(
                                    id = it.id
                            )
                        }
                    }
                    .setCancelButton(getString(R.string.cancel_2)) { dialog ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()

        }

    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "BottomSheetMakeReservation"
        fun newInstance() = BottomSheetMakeReservation()

    }


}