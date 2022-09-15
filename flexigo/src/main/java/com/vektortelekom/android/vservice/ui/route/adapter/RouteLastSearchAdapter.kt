package com.vektortelekom.android.vservice.ui.route.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.LocationModel
import com.vektortelekom.android.vservice.databinding.MenuAddAddressSearchViewHolderBinding
import kotlinx.android.extensions.LayoutContainer

class RouteLastSearchAdapter (var lastSearchRoute: ArrayList<LocationModel>, val listener: SearchItemClickListener?): RecyclerView.Adapter<RouteLastSearchAdapter.SearchResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = MenuAddAddressSearchViewHolderBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return lastSearchRoute.size
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class SearchResultViewHolder (val binding: MenuAddAddressSearchViewHolderBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(position: Int) {

            if(position < lastSearchRoute.size) {
                val item = lastSearchRoute[position]

                binding.textViewAddress.text = item.address

                containerView.setOnClickListener {
                    listener?.onItemClicked(item)
                }

                binding.imageViewAddress.setImageResource(R.drawable.ic_location_map)

            }

        }

    }

    interface SearchItemClickListener {
        fun onItemClicked(model: LocationModel)
    }


}