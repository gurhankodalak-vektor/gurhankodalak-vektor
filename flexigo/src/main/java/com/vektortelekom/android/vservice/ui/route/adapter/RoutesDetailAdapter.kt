package com.vektortelekom.android.vservice.ui.route.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.DestinationModel
import com.vektortelekom.android.vservice.data.model.RouteModel
import com.vektortelekom.android.vservice.databinding.RouteDetailListItemBinding
import kotlinx.android.extensions.LayoutContainer

class RoutesDetailAdapter(var destination: DestinationModel? = null, var onClick: (RouteModel) -> Unit): RecyclerView.Adapter<RoutesDetailAdapter.ShuttleRouteViewHolder>() {

    var routes: MutableList<RouteModel> = mutableListOf()
    var workgroupType: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutesDetailAdapter.ShuttleRouteViewHolder {
        val binding = RouteDetailListItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ShuttleRouteViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    override fun onBindViewHolder(holder: ShuttleRouteViewHolder, position: Int) {
        holder.bind(routes[position])
    }

    inner class ShuttleRouteViewHolder (val binding: RouteDetailListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(route: RouteModel) {

            val minuteText = itemView.context.getString(R.string.short_minute)
            val walkingDurationInMin = route.closestStation?.durationInMin?.toInt()?:0
            val walkingDurationInMinDisplayString = walkingDurationInMin.toString().plus(minuteText)
            binding.textViewRouteName.text = route.name

            binding.textViewRouteFullness.text = "${route.personnelCount}/${route.vehicleCapacity}"

            if (route.closestStation?.title == null)
                binding.textViewRouteNameStop.text = route.destination.name
            else
                binding.textViewRouteNameStop.text = route.destination.name.plus(" - ").plus(route.closestStation?.title)

            binding.textViewRouteArrival.text = "${(walkingDurationInMin) + (route.durationInMin?.toInt() ?: 0)}${minuteText}"

            if (route.durationInMin != null)
                binding.textViewDurationTrip.text = route.durationInMin.toInt().toString().plus(minuteText) ?: "-"
            else
                binding.textViewDurationTrip.text = "0".plus(minuteText)

            binding.textViewDurationWalking.text = walkingDurationInMinDisplayString

            if (workgroupType == "SHUTTLE")
                binding.imageView1.setBackgroundResource(R.drawable.ic_shuttle_bottom_menu_shuttle)
            else
                binding.imageView1.setBackgroundResource(R.drawable.ic_minivan)

            if (walkingDurationInMin == 0){
                binding.textViewDurationWalking.visibility = View.INVISIBLE
                binding.imageViewWalking.visibility = View.INVISIBLE
            } else{
                binding.textViewDurationWalking.visibility = View.VISIBLE
                binding.imageViewWalking.visibility = View.VISIBLE
            }

            containerView.rootView.setOnClickListener {
                onClick(route)
            }

        }

    }

    fun setList(routes: MutableList<RouteModel>, workgroupType: String?) {
        this.routes = routes
        this.workgroupType = workgroupType ?: "SHUTTLE"
        notifyDataSetChanged()
    }

    fun getList() : MutableList<RouteModel> {
        return routes
    }

}