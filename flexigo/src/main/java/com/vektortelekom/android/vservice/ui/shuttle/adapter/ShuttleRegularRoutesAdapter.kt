package com.vektortelekom.android.vservice.ui.shuttle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.ShuttleNextRide
import com.vektortelekom.android.vservice.data.model.WorkgroupDirection
import com.vektortelekom.android.vservice.databinding.ShuttleListItemRegularRoutesBinding
import com.vektortelekom.android.vservice.utils.convertToShuttleDateTime
import kotlinx.android.extensions.LayoutContainer


class ShuttleRegularRoutesAdapter(val listener: ShuttleRegularRouteItemClickListener): RecyclerView.Adapter<ShuttleRegularRoutesAdapter.ShuttleRegularRoutesViewHolder>() {

    private var regularRoutes: List<ShuttleNextRide> = listOf()
    private lateinit var viewBinderHelper: ViewBinderHelper

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShuttleRegularRoutesAdapter.ShuttleRegularRoutesViewHolder {
        val binding = ShuttleListItemRegularRoutesBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return ShuttleRegularRoutesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return regularRoutes.size
    }

    override fun onBindViewHolder(holder: ShuttleRegularRoutesViewHolder, position: Int) {
        holder.bind(regularRoutes[position])
        viewBinderHelper = ViewBinderHelper()
        viewBinderHelper.bind(holder.binding.swipeLayout, regularRoutes[position].routeId.toString())
    }

    inner class ShuttleRegularRoutesViewHolder (val binding: ShuttleListItemRegularRoutesBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(model: ShuttleNextRide) {
            binding.textviewRegularRouteName.text = model.name
            binding.textviewRegularVehiclePlate.text = model.vehiclePlate

            var timeText = model.firstDepartureDate.convertToShuttleDateTime()

            if (model.workgroupDirection == WorkgroupDirection.ROUND_TRIP && model.firstLeg) {
                timeText +=  " -" + model.returnDepartureDate.convertToShuttleDateTime()
            }
            binding.textviewRegularRouteTime.text = timeText

            if (model.notUsing) {
                binding.textviewRegularNotUsing.visibility = View.VISIBLE
                binding.textviewRegularNotUsing.text = " • ".plus(containerView.context.getString(R.string.not_attending))
                binding.buttonCancel.setBackgroundColor(ContextCompat.getColor(containerView.context, R.color.colorAquaGreen))
                binding.buttonCancel.setImageResource(R.drawable.tick)
            } else {
                binding.textviewRegularNotUsing.visibility = View.GONE
                binding.buttonCancel.setBackgroundColor(ContextCompat.getColor(containerView.context, R.color.colorPinkishRed))
                binding.buttonCancel.setImageResource(R.drawable.ic_close_white)

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

    interface ShuttleRegularRouteItemClickListener {
        fun onCancelClicked(model: ShuttleNextRide)
        fun onEditClicked(model: ShuttleNextRide)
    }

    fun setList(regularRoutes: List<ShuttleNextRide>) {
        this.regularRoutes = regularRoutes
        notifyDataSetChanged()
    }

}