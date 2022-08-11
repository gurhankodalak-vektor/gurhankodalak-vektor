package com.vektortelekom.android.vservice.ui.shuttle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.ShuttleNextRide
import com.vektortelekom.android.vservice.data.model.WorkgroupDirection
import com.vektortelekom.android.vservice.databinding.ShuttleListItemDemandBinding
import com.vektortelekom.android.vservice.utils.convertToShuttleDateTime
import kotlinx.android.extensions.LayoutContainer

class ShuttleDemandAdapter: RecyclerView.Adapter<ShuttleDemandAdapter.ShuttleReservationViewHolder>() {

    private var demands: List<ShuttleNextRide> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShuttleDemandAdapter.ShuttleReservationViewHolder {
        val binding = ShuttleListItemDemandBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ShuttleReservationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return demands.size
    }

    override fun onBindViewHolder(holder: ShuttleReservationViewHolder, position: Int) {
        holder.bind(demands[position])
    }

    inner class ShuttleReservationViewHolder (val binding: ShuttleListItemDemandBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(model: ShuttleNextRide) {
            var timeText = model.firstDepartureDate.convertToShuttleDateTime()

            if(model.workgroupDirection == WorkgroupDirection.ROUND_TRIP) {
                timeText = timeText.plus(" - ").plus(model.returnDepartureDate.convertToShuttleDateTime())
            }
            binding.textViewTime.text = timeText
        }

    }

    fun setList(demands: List<ShuttleNextRide>) {
        this.demands = demands
        notifyDataSetChanged()
    }

}