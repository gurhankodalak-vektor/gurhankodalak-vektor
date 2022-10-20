package com.vektortelekom.android.vservice.ui.carpool.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.DestinationModel
import com.vektortelekom.android.vservice.data.model.RouteModel
import com.vektortelekom.android.vservice.databinding.FlexiCarpoolListItemBinding
import kotlinx.android.extensions.LayoutContainer

class CarPoolAdapter(var destination: DestinationModel? = null, var onClick: (RouteModel) -> Unit): RecyclerView.Adapter<CarPoolAdapter.ViewHolder>() {

    var routes: MutableList<RouteModel> = mutableListOf()
    var workgroupType: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarPoolAdapter.ViewHolder {
        val binding = FlexiCarpoolListItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(routes[position])
    }

    inner class ViewHolder (val binding: FlexiCarpoolListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(route: RouteModel) {

            containerView.rootView.setOnClickListener {
                onClick(route)
            }

        }
    }

    fun setList(routes: MutableList<RouteModel>) {
        this.routes = routes
        notifyDataSetChanged()
    }

    fun getList() : MutableList<RouteModel> {
        return routes
    }

    fun setMyDestination(destination: DestinationModel?) {
        this.destination = destination
        notifyDataSetChanged()
    }

}