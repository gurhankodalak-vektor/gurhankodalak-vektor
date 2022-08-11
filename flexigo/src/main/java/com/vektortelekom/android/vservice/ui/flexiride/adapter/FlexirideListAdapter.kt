package com.vektortelekom.android.vservice.ui.flexiride.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.FlexirideViewHolderItemBinding
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.utils.convertBackendDateToLong
import com.vektortelekom.android.vservice.utils.convertBackendDateToReservationString
import com.vektortelekom.android.vservice.utils.convertForBackend2
import com.vektortelekom.android.vservice.utils.dpToPx
import kotlinx.android.extensions.LayoutContainer
import org.joda.time.DateTime

class FlexirideListAdapter (private val flexirideList: List<PoolcarAndFlexirideModel>, val listener: FlexirideItemListener)  : RecyclerView.Adapter<FlexirideListAdapter.FlexirideListViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexirideListViewHolder {
        val binding = FlexirideViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return FlexirideListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlexirideListViewHolder, position: Int) {
        holder.bind(flexirideList[position], position)
    }

    override fun getItemCount(): Int {
        return flexirideList.size
    }

    inner class FlexirideListViewHolder ( val binding: FlexirideViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(flexiride: PoolcarAndFlexirideModel, position: Int) {

            when(flexiride.requestType) {
                FlexirideAndPoolcarRequestType.GUESTRIDE -> {
                    binding.textViewTitle.text = containerView.context.getString(R.string.flexiride_list_title_guest)

                    binding.textViewGuestPhone.visibility = View.VISIBLE
                    binding.textViewGuestNameInfo.visibility = View.VISIBLE
                    binding.textViewGuestName.visibility = View.VISIBLE

                    if(flexiride.flexirideRequest?.fullName == null) {
                        binding.textViewGuestName.text = containerView.context.getString(R.string.waiting)
                    }
                    else {
                        binding.textViewGuestName.text = flexiride.flexirideRequest?.fullName
                    }

                    if(flexiride.flexirideRequest?.mobile == null) {
                        binding.textViewGuestPhone.text = containerView.context.getString(R.string.waiting)

                        binding.buttonCallGuest.visibility = View.GONE
                    }
                    else {
                        binding.textViewGuestPhone.text = flexiride.flexirideRequest?.mobile
                        binding.buttonCallGuest.visibility = View.VISIBLE

                        binding.buttonCallGuest.setOnClickListener {
                            AppDialog.Builder(binding.buttonCallGuest.context)
                                    .setCloseButtonVisibility(false)
                                    .setIconVisibility(false)
                                    .setTitle("Ara")
                                    .setSubtitle(binding.buttonCallGuest.context.getString(R.string.will_call, flexiride.flexirideRequest?.mobile))
                                    .setOkButton(binding.buttonCallGuest.context.getString(R.string.Generic_Ok)) { d ->
                                        d.dismiss()
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(flexiride.flexirideRequest?.mobile)))
                                        binding.buttonCallGuest.context.startActivity(intent)
                                    }
                                    .setCancelButton("İptal") { d ->
                                        d.dismiss()
                                    }
                                    .create().show()
                        }
                    }

                }
                else -> {
                    binding.textViewTitle.text = containerView.context.getString(R.string.flexiride_list_title_normal)

                    binding.layoutGuestPhone.visibility = View.GONE
                    binding.textViewGuestNameInfo.visibility = View.GONE
                    binding.textViewGuestName.visibility = View.GONE

                }
            }



            binding.buttonCancel.setOnClickListener {
                listener.deleteFlexiride(flexiride)
            }

            binding.buttonStart.setOnClickListener {
                listener.flexirideSelected(flexiride)
            }

            binding.buttonSurvey.setOnClickListener {
                listener.evaluateFlexiride(flexiride)
            }

            when(flexiride.status) {
                FlexirideAndPoolcarStatus.PLANNED -> {
                    binding.buttonStart.visibility = View.VISIBLE
                    binding.buttonCancel.visibility = View.VISIBLE
                    binding.buttonSurvey.visibility = View.GONE
                    binding.viewDivider2.visibility = View.VISIBLE
                    binding.cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(containerView.context, R.color.colorPrimary))
                    binding.textViewStatus.text = containerView.context.getString(R.string.planned)

                    val layoutParamsCancel = binding.buttonCancel.layoutParams
                    if(layoutParamsCancel is ConstraintLayout.LayoutParams) {
                        layoutParamsCancel.marginStart = 12f.dpToPx(containerView.context)
                    }
                    binding.buttonCancel.layoutParams = layoutParamsCancel
                }
                FlexirideAndPoolcarStatus.APPROVED -> {
                    binding.buttonStart.visibility = View.VISIBLE
                    binding.buttonCancel.visibility = View.VISIBLE
                    binding.buttonSurvey.visibility = View.GONE
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
                    binding.buttonSurvey.visibility = View.GONE
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
                    binding.buttonSurvey.visibility = View.GONE
                    binding.viewDivider2.visibility = View.GONE
                    binding.cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(containerView.context, R.color.watermelon))
                    binding.textViewStatus.text = containerView.context.getString(R.string.rejected)
                }
                FlexirideAndPoolcarStatus.CANCELLED -> {
                    binding.buttonStart.visibility = View.GONE
                    binding.buttonCancel.visibility = View.GONE
                    binding.buttonSurvey.visibility = View.GONE
                    binding.viewDivider2.visibility = View.GONE
                    binding.cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(containerView.context, R.color.watermelon))
                    binding.textViewStatus.text = containerView.context.getString(R.string.cancelled)
                }
                FlexirideAndPoolcarStatus.FINISHED -> {
                    binding.buttonStart.visibility = View.GONE
                    binding.buttonCancel.visibility = View.GONE
                    binding.buttonSurvey.visibility = View.VISIBLE
                    binding.viewDivider2.visibility = View.GONE
                    binding.cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(containerView.context, R.color.watermelon))
                    binding.textViewStatus.text = containerView.context.getString(R.string.finished)
                }
            }

            binding.textViewPickUp.text = flexiride.flexirideRequest?.requestedPickupTime.convertBackendDateToReservationString()

            if(flexiride.flexirideRequest?.travelTimeInMinute == null) {
                binding.textViewEstimatedArrivalTime.text = containerView.context.getString(R.string.waiting)
            }
            else {
                binding.textViewEstimatedArrivalTime.text = DateTime(flexiride.flexirideRequest?.requestedPickupTime.convertBackendDateToLong()).plusMinutes(flexiride.flexirideRequest?.travelTimeInMinute?.toInt()?:0).toDate()
                        .convertForBackend2()
                        .convertBackendDateToReservationString()
            }

            if(flexiride.driver?.fullName == null) {
                binding.textViewDriver.text = containerView.context.getString(R.string.waiting)
            }
            else {
                binding.textViewDriver.text = flexiride.driver?.fullName
            }

            if(flexiride.driver?.mobile == null) {
                binding.buttonCallDriver.visibility = View.GONE
            }
            else {
                binding.buttonCallDriver.visibility = View.VISIBLE

                binding.buttonCallDriver.setOnClickListener {
                    AppDialog.Builder(binding.buttonCallDriver.context)
                            .setCloseButtonVisibility(false)
                            .setIconVisibility(false)
                            .setTitle("Ara")
                            .setSubtitle(binding.buttonCallDriver.context.getString(R.string.will_call, flexiride.driver?.mobile))
                            .setOkButton(binding.buttonCallDriver.context.getString(R.string.Generic_Ok)) { d ->
                                d.dismiss()
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(flexiride.driver?.mobile)))
                                binding.buttonCallDriver.context.startActivity(intent)
                            }
                            .setCancelButton(binding.buttonCallDriver.context.getString(R.string.cancel)) { d ->
                                d.dismiss()
                            }
                            .create().show()
                }

            }


            if(flexiride.vehicle?.plate == null) {
                binding.textViewVehicle.text = containerView.context.getString(R.string.waiting)
            }
            else {
                binding.textViewVehicle.text = flexiride.vehicle?.plate
            }

        }
    }

    interface FlexirideItemListener {
        fun flexirideSelected(flexiride: PoolcarAndFlexirideModel)
        fun deleteFlexiride(flexiride: PoolcarAndFlexirideModel)
        fun evaluateFlexiride(flexiride: PoolcarAndFlexirideModel)
    }


}