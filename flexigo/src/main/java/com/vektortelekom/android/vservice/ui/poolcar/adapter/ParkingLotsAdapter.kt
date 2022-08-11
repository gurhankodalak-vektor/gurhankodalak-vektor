package com.vektortelekom.android.vservice.ui.poolcar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.ParkModel
import com.vektortelekom.android.vservice.databinding.PoolCarViewHolderItemBinding
import kotlinx.android.extensions.LayoutContainer

class ParkingLotsAdapter (private val parkingLots: List<ParkModel>, val listener: ParkListener?) : RecyclerView.Adapter<ParkingLotsAdapter.ParkingLotsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkingLotsViewHolder {
        val binding = PoolCarViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ParkingLotsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return parkingLots.size
    }

    override fun onBindViewHolder(holder: ParkingLotsViewHolder, position: Int) {
        holder.bind(parkingLots[position])
    }

    inner class ParkingLotsViewHolder (val binding: PoolCarViewHolderItemBinding) : RecyclerView.ViewHolder(binding.rootView), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(parkingLot: ParkModel) {

            binding.textViewParkingLotsName.text = parkingLot.name
            binding.textViewOpeningHours.text = parkingLot.workingHours
            binding.textViewAvailableForRent.text = containerView.context.getString(R.string.count_suitable_car, parkingLot.vehicleAvailableCount)

            binding.rootView.setOnClickListener {
                listener?.parkSelected(parkingLot)
            }

        }

    }

    interface ParkListener {
        fun parkSelected(parkModel: ParkModel)
    }

}