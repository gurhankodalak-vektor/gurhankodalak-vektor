package com.vektortelekom.android.vservice.ui.shuttle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.RouteModel
import com.vektortelekom.android.vservice.databinding.MenuAddAddressSearchViewHolderBinding
import kotlinx.android.extensions.LayoutContainer

class SearchFromToPlaceResultsAdapter (var searchResultsAutocompletePrediction : List<AutocompletePrediction>, var searchResultsRoute: List<RouteModel>, val listener: SearchItemClickListener?): RecyclerView.Adapter<SearchFromToPlaceResultsAdapter.SearchResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = MenuAddAddressSearchViewHolderBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return searchResultsAutocompletePrediction.size + searchResultsRoute.size
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(position)
    }

    fun setSearchList(searchResultsAutocompletePrediction : List<AutocompletePrediction>, searchResultsRoute: List<RouteModel>) {
        this.searchResultsAutocompletePrediction = searchResultsAutocompletePrediction
        this.searchResultsRoute = searchResultsRoute
        notifyDataSetChanged()
    }

    inner class SearchResultViewHolder (val binding: MenuAddAddressSearchViewHolderBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(position: Int) {

            if(position < searchResultsRoute.size) {
                val route = searchResultsRoute[position]

                binding.textViewAddress.text = route.name

                containerView.setOnClickListener {
                    listener?.onItemClicked(route)
                }

                binding.imageViewAddress.setImageResource(R.drawable.ic_location_map)

            }
            else {
                val autocompletePrediction = searchResultsAutocompletePrediction[position - searchResultsRoute.size]

                binding.textViewAddress.text = autocompletePrediction.getFullText(null)

                containerView.setOnClickListener {
                    listener?.onItemClicked(autocompletePrediction)
                }

                if(position == searchResultsAutocompletePrediction.size+searchResultsRoute.size - 1) {
                    binding.viewDivider.visibility = View.GONE
                }
                else {
                    binding.viewDivider.visibility = View.VISIBLE
                }

                binding.imageViewAddress.setImageResource(R.drawable.ic_marker)
                binding.imageViewAddress.background = null

            }

        }

    }

    interface SearchItemClickListener {
        fun onItemClicked(autocompletePrediction: AutocompletePrediction)
        fun onItemClicked(route: RouteModel)
    }


}