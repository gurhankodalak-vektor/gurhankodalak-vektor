package com.vektortelekom.android.vservice.ui.poolcar.reservation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.PoolCarReservationsFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.poolcar.reservation.PoolCarReservationViewModel
import com.vektortelekom.android.vservice.ui.poolcar.reservation.adapter.ReservationsAdapter
import com.vektortelekom.android.vservice.ui.poolcar.reservation.dialog.StartReservationDialog
import javax.inject.Inject

class PoolCarReservationsFragment : BaseFragment<PoolCarReservationViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarReservationViewModel

    private lateinit var binding: PoolCarReservationsFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarReservationsFragmentBinding>(inflater, R.layout.pool_car_reservations_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarReservationsFragment
            viewModel = this@PoolCarReservationsFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.reservations.observe(viewLifecycleOwner) { reservations ->

            val reservationList = mutableListOf<PoolcarAndFlexirideModel>()

            var countAccepted = 0
            var countPending = 0
            var countRejected = 0
            for (reservation in reservations) {
                when (reservation.status) {
                    FlexirideAndPoolcarStatus.APPROVED -> {
                        countAccepted++
                        reservationList.add(reservation)
                    }
                    FlexirideAndPoolcarStatus.PENDING -> {
                        countPending++
                        reservationList.add(reservation)
                    }
                    FlexirideAndPoolcarStatus.REJECTED -> {
                        countRejected++
                        reservationList.add(reservation)
                    }
                    else -> {

                    }
                }
            }
            binding.textViewAcceptCount.text = countAccepted.toString()
            binding.textViewExpectantCount.text = countPending.toString()
            binding.textViewRejectedCount.text = countRejected.toString()

            viewModel.reservationStartPrecheck.observe(viewLifecycleOwner) {
                if (it) {
                    viewModel.startReservationModel?.let { reservation ->
                        if (reservation.flexirideRequest?.workflowType == ReservationWorkFlowType.LIGHT) {
                            viewModel.selectedReservationToStart.value = reservation
                        }
                        else {
                            if (reservation.requestType == FlexirideAndPoolcarRequestType.POOL_CAR) {
                                StartReservationDialog(requireContext(), reservation, {
                                    viewModel.selectedReservationToStart.value = reservation
                                }, {
                                    viewModel.reservationIdToUpdateWithQr = reservation.id
                                    viewModel.navigator?.showQrFragment()

                                }).show()
                            } else {
                                viewModel.selectedReservationToStart.value = reservation
                            }
                        }
                    }
                }
            }

            binding.recyclerViewReservations.adapter = ReservationsAdapter(reservationList, object : ReservationsAdapter.ReservationListener {
                override fun cancelReservation(reservation: PoolcarAndFlexirideModel) {
                    viewModel.cancelReservation(reservation.id ?: 0)
                }

                override fun selectReservation(reservation: PoolcarAndFlexirideModel) {
                    viewModel.startReservationModel = reservation
                    reservation.id?.let { reservationId ->
                        viewModel.rentalReservationStartPrecheck(reservationId)
                    }
                }

            })
        }

        viewModel.cancelReservationSuccess.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.getReservations()

                viewModel.cancelReservationSuccess.value = null
            }
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.getReservations()
    }

    override fun getViewModel(): PoolCarReservationViewModel {

        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarReservationViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarReservationsFragment"

        fun newInstance() = PoolCarReservationsFragment()

    }

}