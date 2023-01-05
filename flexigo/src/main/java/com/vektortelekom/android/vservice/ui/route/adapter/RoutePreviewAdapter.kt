package com.vektortelekom.android.vservice.ui.route.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.RouteModel
import com.vektortelekom.android.vservice.databinding.RoutePreviewListItemBinding
import kotlinx.android.extensions.LayoutContainer

class RoutePreviewAdapter(var listener: RoutePreviewListener) : RecyclerView.Adapter<RoutePreviewAdapter.ShuttleRouteViewHolder>() {

    var routes: MutableList<RouteModel> = mutableListOf()
    var pageName: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShuttleRouteViewHolder {
        val binding = RoutePreviewListItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ShuttleRouteViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    override fun onBindViewHolder(holder: ShuttleRouteViewHolder, position: Int) {
        holder.bind(routes[position])
    }


    inner class ShuttleRouteViewHolder(val binding: RoutePreviewListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(route: RouteModel) {

            val minuteText = containerView.context.getString(R.string.short_minute)
            val walkingDurationInMin = route.closestStation?.durationInMin?.toInt() ?: 0
            val walkingDurationInMinDisplayString = walkingDurationInMin.toString().plus(minuteText)

            binding.textViewBottomSheetRouteName.text = route.title
            binding.textViewBottomSheetShuttleFullnessValue.text = "${route.personnelCount}/${route.vehicleCapacity}"
            binding.textViewBottomSheetShuttleRoute.visibility = View.GONE

            binding.textViewBottomSheetArrivalTimeValue.text = "${(walkingDurationInMin) + (route?.durationInMin?.toInt() ?: 0)}${minuteText}"
            binding.textViewDurationTrip.text =  String.format("%.1f", route.durationInMin ?: 0.0).plus(minuteText)
            binding.textViewDurationWalking.text = walkingDurationInMinDisplayString

            if (pageName == "RoutePreview")
                binding.buttonBottomSheetCommunicateWithDriver.visibility = View.INVISIBLE
            else
                binding.buttonBottomSheetCommunicateWithDriver.visibility = View.VISIBLE


            binding.buttonBottomSheetSeeStops.setOnClickListener {
                listener.seeStopsClick(route)
            }
            binding.buttonBottomSheetCommunicateWithDriver.setOnClickListener {
                listener.callDriverClick(route.driver.phoneNumber)
            }
        }

    }

    fun setList(routes: MutableList<RouteModel>) {
        this.routes = routes
        notifyDataSetChanged()
    }

    @JvmName("setPageName1")
    fun setPageName(pageName: String) {
        this.pageName = pageName
    }

    interface RoutePreviewListener {
        fun seeStopsClick(route: RouteModel)
        fun callDriverClick(phoneNumber: String?)
    }

}