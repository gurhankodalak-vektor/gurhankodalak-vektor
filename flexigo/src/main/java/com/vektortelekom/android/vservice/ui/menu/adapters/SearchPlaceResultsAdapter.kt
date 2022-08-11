package com.vektortelekom.android.vservice.ui.menu.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.vektortelekom.android.vservice.databinding.MenuAddAddressSearchViewHolderBinding
import kotlinx.android.extensions.LayoutContainer

class SearchPlaceResultsAdapter (var searchResults : List<AutocompletePrediction>, val listener: SearchItemClickListener?): RecyclerView.Adapter<SearchPlaceResultsAdapter.SearchResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = MenuAddAddressSearchViewHolderBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(searchResults[position], position)
    }

    fun setSearchList(searchResults : List<AutocompletePrediction>) {
        this.searchResults = searchResults
        notifyDataSetChanged()
    }

    inner class SearchResultViewHolder (val binding: MenuAddAddressSearchViewHolderBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(searchResult : AutocompletePrediction, position: Int) {
            binding.textViewAddress.text = searchResult.getFullText(null)

            containerView.setOnClickListener {
                listener?.onItemClicked(searchResult)
            }

            if(position == searchResults.size - 1) {
                binding.viewDivider.visibility = View.GONE
            }
            else {
                binding.viewDivider.visibility = View.VISIBLE
            }

        }

    }

    interface SearchItemClickListener {
        fun onItemClicked(autocompletePrediction: AutocompletePrediction)
    }


}