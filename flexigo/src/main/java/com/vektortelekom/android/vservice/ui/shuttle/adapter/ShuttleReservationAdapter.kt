package com.vektortelekom.android.vservice.ui.shuttle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.ShuttleListItemBinding
import com.vektortelekom.android.vservice.utils.convertToShuttleDateTime
import kotlinx.android.extensions.LayoutContainer

class ShuttleReservationAdapter(val listener: ShuttleReservationItemClickListener): RecyclerView.Adapter<ShuttleReservationAdapter.ShuttleReservationViewHolder>() {

    private var reservations: List<ShuttleNextRide> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShuttleReservationAdapter.ShuttleReservationViewHolder {
        val binding = ShuttleListItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return ShuttleReservationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reservations.size
    }

    override fun onBindViewHolder(holder: ShuttleReservationViewHolder, position: Int) {
        holder.bind(reservations[position])
    }

    inner class ShuttleReservationViewHolder (val binding: ShuttleListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(model: ShuttleNextRide) {

            if (model.workgroupStatus == WorkgroupStatus.PENDING_PLANNING || model.workgroupStatus == WorkgroupStatus.PENDING_DEMAND){
                binding.textviewPlanningProcess.visibility = View.VISIBLE
                binding.textViewPlate.visibility = View.GONE
            } else{
                binding.textviewPlanningProcess.visibility = View.GONE
                binding.textViewPlate.visibility = View.VISIBLE
            }


            if (model.workgroupType == "SHUTTLE")
                binding.imageviewCar.setBackgroundResource(R.drawable.ic_shuttle_bottom_menu_shuttle)
            else
                binding.imageviewCar.setBackgroundResource(R.drawable.ic_minivan)

            val timeText = model.firstDepartureDate.convertToShuttleDateTime()

//            if(model.workgroupDirection == WorkgroupDirection.ROUND_TRIP) {
//                timeText = timeText.plus(" - ").plus(model.returnDepartureDate.convertToShuttleDateTime())
//            }

            binding.textViewTime.text = containerView.context.getString(R.string.departure_not_dot, timeText)

            if (model.reserved) {
                binding.textViewRegularRouteName.text = model.routeName
                binding.textViewPlate.text = model.vehiclePlate
            } else
            {
                binding.textViewRegularRouteName.text = model.name
                binding.textViewPlate.visibility = View.GONE
            }

            binding.imageButtonEdit.setOnClickListener {
                listener.onEditClicked(model)
                binding.swipeLayout.close(true)
            }
            binding.buttonCancel.setOnClickListener {
                listener.onCancelClicked(model)
                binding.swipeLayout.close(true)
            }

        }

    }

    interface ShuttleReservationItemClickListener {
        fun onCancelClicked(model: ShuttleNextRide)
        fun onEditClicked(model: ShuttleNextRide)
    }

    fun setList(reservations: List<ShuttleNextRide>) {
        this.reservations = reservations
        notifyDataSetChanged()
    }

}