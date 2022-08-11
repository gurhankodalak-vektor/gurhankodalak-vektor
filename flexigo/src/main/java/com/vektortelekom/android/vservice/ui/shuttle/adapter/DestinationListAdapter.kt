package com.vektortelekom.android.vservice.ui.shuttle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.DestinationModel
import com.vektortelekom.android.vservice.databinding.ShuttleDestinationViewHolderItemBinding
import kotlinx.android.extensions.LayoutContainer

class DestinationListAdapter(val destinations: List<DestinationModel>, val listener: DestinationItemClickListener?) : RecyclerView.Adapter<DestinationListAdapter.DestinationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val binding = ShuttleDestinationViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return DestinationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return destinations.size
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(destinations[position], position)
    }

    inner class DestinationViewHolder (val binding: ShuttleDestinationViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(destination: DestinationModel, position: Int) {
            binding.textViewAddress.text = destination.title

            containerView.setOnClickListener {
                listener?.onItemClicked(destination)
            }

            if(position == destinations.size-1) {
                binding.viewDivider.visibility = View.GONE
            }
            else {
                binding.viewDivider.visibility = View.VISIBLE
            }
        }

    }

    interface DestinationItemClickListener {
        fun onItemClicked(destination: DestinationModel)
    }

}