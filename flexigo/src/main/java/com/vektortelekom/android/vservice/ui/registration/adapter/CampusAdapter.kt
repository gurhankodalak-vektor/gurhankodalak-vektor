package com.vektortelekom.android.vservice.ui.registration.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.DestinationModel
import com.vektortelekom.android.vservice.databinding.CampusListItemBinding
import kotlinx.android.extensions.LayoutContainer

class CampusAdapter(val listener: ItemClickListener): RecyclerView.Adapter<CampusAdapter.ViewHolder>() {

    private var destinations: List<DestinationModel> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampusAdapter.ViewHolder {
        val binding = CampusListItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return destinations.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(destinations[position])
    }

    inner class ViewHolder (val binding: CampusListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(model: DestinationModel) {
            binding.chipCampusName.text = model.name
        }

    }

    fun setList(destinations: List<DestinationModel>) {
        this.destinations = destinations
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onItemClicked(destination: DestinationModel)
    }

}