package com.vektortelekom.android.vservice.ui.shuttle.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.ShuttleDayModel
import com.vektortelekom.android.vservice.data.model.ShuttleReservationCancelRequest
import com.vektortelekom.android.vservice.databinding.ShuttleInformationFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.shuttle.adapter.ShuttleDayAdapter
import com.vektortelekom.android.vservice.ui.shuttle.dialog.ShuttleReservationCancelDialog
import com.vektortelekom.android.vservice.utils.convertToShuttleDayTitle
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ShuttleInformationFragment : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: ShuttleInformationFragmentBinding

    private var shuttleDayAdapter : ShuttleDayAdapter? = null

    private val dayInterval = 15

    private var startDate: Date = Date()
    private var endDate: Date = DateTime(startDate).plusDays(dayInterval).toDate()

    private var shuttleReservationCancelDialog: ShuttleReservationCancelDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<ShuttleInformationFragmentBinding>(inflater, R.layout.shuttle_information_fragment, container, false).apply {
            lifecycleOwner = this@ShuttleInformationFragment
            viewModel = this@ShuttleInformationFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDayPrev.setOnClickListener {
            previousTapped()
        }

        binding.buttonDayNext.setOnClickListener {
            nextTapped()
        }

        previousTapped()

        shuttleDayAdapter = ShuttleDayAdapter(ArrayList(), object: ShuttleDayAdapter.ShuttleItemListener {
            override fun shuttleItemClicked(model: ShuttleDayModel) {
                viewModel.updateShuttleDay(model)
            }

            override fun shuttleMorningReservationClicked(model: ShuttleDayModel) {

                viewModel.selectedShuttleDay = model
                viewModel.isShuttleDayMorning = true

                model.bookedIncomingRoute?.id?.let {
                    viewModel.getRouteDetailsForAttendanceReservation(it.toLong())
                }

            }

            override fun shuttleEveningReservationClicked(model: ShuttleDayModel) {

                viewModel.selectedShuttleDay = model
                viewModel.isShuttleDayMorning = false

                model.bookedOutgoingRoute?.id?.let {
                    viewModel.getRouteDetailsForAttendanceReservation(it.toLong())
                }
            }

            override fun shuttleOvertimeItemClicked(model: ShuttleDayModel) {
                model.isOutgoing = null
                model.isIncoming = null
                viewModel.updateShuttleDay(model)
            }

        })

        binding.recyclerViewShuttleDay.adapter = shuttleDayAdapter

        viewModel.getShuttleUseDaysResponse.observe(viewLifecycleOwner, Observer { response ->

            shuttleDayAdapter?.setList(response.response)

        })

        viewModel.updateShuttleDayResult.observe(viewLifecycleOwner, Observer {
            shuttleDayAdapter?.notifyDataSetChanged()
        })

        viewModel.reservationCancelled.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                if(shuttleReservationCancelDialog?.isShowing == true) {
                    shuttleReservationCancelDialog?.dismiss()
                    shuttleReservationCancelDialog = null
                }
                viewModel.reservationCancelled.value = null
                FlexigoInfoDialog.Builder(requireContext())
                        .setIconVisibility(false)
                        .setTitle(getString(R.string.shuttle_reservation_dialog_title))
                        .setText1(getString(R.string.shuttle_reservation_dialog_cancel_text))
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog1 ->
                            dialog1.dismiss()

                        }
                        .create().show()
                getShuttleUseDays()
            }
        })

        viewModel.routeDetailsForAttendenceReservation.observe(viewLifecycleOwner) {

            if(it == null) {
                return@observe
            }

            viewModel.selectedShuttleDay?.let { model ->

                viewModel.isShuttleDayMorning?.let { isMorning ->

                    shuttleReservationCancelDialog = ShuttleReservationCancelDialog(requireContext(), model, it, object : ShuttleReservationCancelDialog.ShuttleReservationCancelListener {
                        override fun cancelReservation() {
                            val request = ShuttleReservationCancelRequest(
                                    bookingDay = model.shuttleDay,
                                    isIncoming = isMorning,
                                    isOutgoing = isMorning.not(),
                                    routeId = if(isMorning) model.bookedIncomingRoute?.id?:0 else model.bookedOutgoingRoute?.id?:0,
                                    stationId = if(isMorning) model.bookedIncomingStation?.id?:0 else model.bookedOutgoingStation?.id?:0
                            )
                            viewModel.cancelShuttleReservation(request)
                        }

                    })
                    shuttleReservationCancelDialog?.show()
                }

            }

            viewModel.routeDetailsForAttendenceReservation.value = null


        }


    }

    private fun nextTapped() {
        binding.buttonDayPrev.visibility = View.VISIBLE
        binding.buttonDayNext.visibility = View.INVISIBLE
        startDate = endDate
        endDate = DateTime(startDate).plusDays(dayInterval).toDate()
        viewModel.startDate = startDate
        viewModel.endDate = endDate
        setTitle()
        getShuttleUseDays()
    }

    private fun previousTapped() {
        binding.buttonDayPrev.visibility = View.INVISIBLE
        binding.buttonDayNext.visibility = View.VISIBLE
        startDate = Date()
        endDate = DateTime(startDate).plusDays(dayInterval).toDate()
        viewModel.startDate = startDate
        viewModel.endDate = endDate
        setTitle()
        getShuttleUseDays()
    }

    private fun setTitle() {
        binding.textViewTimeInterval.text = startDate.convertToShuttleDayTitle().plus(" - ").plus(endDate.convertToShuttleDayTitle())
    }

    private fun getShuttleUseDays() {
        viewModel.getShuttleUseDays()
    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "ShuttleInformationFragment"

        fun newInstance() = ShuttleInformationFragment()

    }

}