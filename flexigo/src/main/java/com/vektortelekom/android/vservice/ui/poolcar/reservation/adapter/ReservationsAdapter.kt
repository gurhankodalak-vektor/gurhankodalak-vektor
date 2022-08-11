package com.vektortelekom.android.vservice.ui.poolcar.reservation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.PoolCarReservationsViewHolderItemBinding
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.utils.convertBackendDateToReservationString
import com.vektortelekom.android.vservice.utils.dpToPx
import kotlinx.android.extensions.LayoutContainer

class ReservationsAdapter(private val reservations: List<PoolcarAndFlexirideModel>, private val listener: ReservationListener?) : RecyclerView.Adapter<ReservationsAdapter.ReservationsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationsViewHolder {
        val binding = PoolCarReservationsViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ReservationsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reservations.size
    }

    override fun onBindViewHolder(holder: ReservationsViewHolder, position: Int) {
        holder.bind(reservations[position])
    }

    inner class ReservationsViewHolder (val binding: PoolCarReservationsViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(reservation: PoolcarAndFlexirideModel) {

            binding.textViewTitle.text = if(reservation.travelDestinationType == TravelDestinationType.INTERCITY) containerView.context.getString(R.string.make_reservation_intercity)
                                     else containerView.context.getString(R.string.make_reservation_local)

            var vehicleText = reservation.vehicle?.plate

            reservation.vehicle?.make?.let {  make ->
                vehicleText = vehicleText.plus(" - ")
                        .plus(make)
                reservation.vehicle?.model?.let { model ->
                    vehicleText = vehicleText.plus(" ")
                            .plus(model)
                }
            }

            if(vehicleText.isNullOrBlank()) {
                vehicleText = containerView.context.getString(R.string.vehicle_not_defined_yet)
            }

            binding.textViewVehicle.text = vehicleText

            binding.textViewStartDate.text = reservation.flexirideRequest?.requestedPickupTime.convertBackendDateToReservationString()

            binding.textViewEndDate.text = reservation.flexirideRequest?.requestedDeliveryTime.convertBackendDateToReservationString()

            binding.textViewDescription.text = reservation.reservation?.description

            binding.textViewFrom.text = if(reservation.requestType == FlexirideAndPoolcarRequestType.PARTNER_CAR_REQUEST) reservation.fromLocation?.name else reservation.park?.name
            binding.textViewTo.text = reservation.toLocation?.name

            binding.textViewTo.visibility = if(reservation.requestType == FlexirideAndPoolcarRequestType.PARTNER_CAR_REQUEST) View.VISIBLE else View.GONE
            binding.textViewToInfo.visibility = if(reservation.requestType == FlexirideAndPoolcarRequestType.PARTNER_CAR_REQUEST) View.VISIBLE else View.GONE

            when(reservation.status) {
                FlexirideAndPoolcarStatus.APPROVED -> {
                    binding.buttonStart.visibility = View.VISIBLE
                    binding.buttonCancel.visibility = View.VISIBLE
                    binding.viewDivider2.visibility = View.VISIBLE
                    binding.cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(containerView.context, R.color.colorPrimary))
                    binding.textViewStatus.text = containerView.context.getString(R.string.accept)

                    val layoutParamsCancel = binding.buttonCancel.layoutParams
                    if(layoutParamsCancel is ConstraintLayout.LayoutParams) {
                        layoutParamsCancel.marginStart = 12f.dpToPx(containerView.context)
                    }
                    binding.buttonCancel.layoutParams = layoutParamsCancel
                }
                FlexirideAndPoolcarStatus.PENDING -> {
                    binding.buttonStart.visibility = View.GONE
                    binding.buttonCancel.visibility = View.VISIBLE
                    binding.viewDivider2.visibility = View.VISIBLE
                    binding.cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(containerView.context, R.color.marigold))
                    binding.textViewStatus.text = containerView.context.getString(R.string.expectant)

                    val layoutParamsCancel = binding.buttonCancel.layoutParams
                    if(layoutParamsCancel is ConstraintLayout.LayoutParams) {
                        layoutParamsCancel.marginStart = 0f.dpToPx(containerView.context)
                    }
                    binding.buttonCancel.layoutParams = layoutParamsCancel
                }
                FlexirideAndPoolcarStatus.REJECTED -> {
                    binding.buttonStart.visibility = View.GONE
                    binding.buttonCancel.visibility = View.GONE
                    binding.viewDivider2.visibility = View.GONE
                    binding.cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(containerView.context, R.color.watermelon))
                    binding.textViewStatus.text = containerView.context.getString(R.string.rejected)
                }
                FlexirideAndPoolcarStatus.CANCELLED -> {
                    binding.buttonStart.visibility = View.GONE
                    binding.buttonCancel.visibility = View.GONE
                    binding.viewDivider2.visibility = View.GONE
                    binding.cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(containerView.context, R.color.watermelon))
                    binding.textViewStatus.text = containerView.context.getString(R.string.cancelled)
                }
            }

            binding.buttonCancel.setOnClickListener {

                FlexigoInfoDialog.Builder(it.context)
                        .setTitle(it.context.getString(R.string.are_you_sure))
                        .setText1(it.context.getString(R.string.cancel_reservation_text))
                        .setCancelable(true)
                        .setIconVisibility(false)
                        .setOkButton(it.context.getString(R.string.yes)) { dialog ->
                            dialog.dismiss()
                            listener?.cancelReservation(reservation)
                        }
                        .setCancelButton(it.context.getString(R.string.no)) { dialog ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()

            }

            binding.buttonStart.setOnClickListener {
                listener?.selectReservation(reservation)
            }

        }

    }

    interface ReservationListener {
        fun cancelReservation(reservation: PoolcarAndFlexirideModel)
        fun selectReservation(reservation: PoolcarAndFlexirideModel)
    }

}