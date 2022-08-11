package com.vektortelekom.android.vservice.ui.flexiride.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.FlexirideOffer
import com.vektortelekom.android.vservice.databinding.FlexirideOffersViewHolderItemBinding
import kotlinx.android.extensions.LayoutContainer

class FlexirideOfferListAdapter(private val offers: List<FlexirideOffer>, val listener: OfferSelectListener?) : RecyclerView.Adapter<FlexirideOfferListAdapter.FlexirideOfferViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexirideOfferViewHolder {
        val binding = FlexirideOffersViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return FlexirideOfferViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return offers.size
    }

    override fun onBindViewHolder(holder: FlexirideOfferViewHolder, position: Int) {
        holder.bind(offers[position])
    }

    inner class FlexirideOfferViewHolder (val binding: FlexirideOffersViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {

        override val containerView: View
            get() = binding.root

        fun bind(offer: FlexirideOffer) {

            binding.textViewFromTime.text = offer.startTime
            binding.textViewFromAddress.text = offer.startLocation
            binding.textViewToTime.text = offer.finishTime
            binding.textViewToAddress.text = offer.finishLocation
            binding.textViewWalking.text = containerView.context.getString(R.string.have_to_walk, offer.walkingMin)
            binding.textViewPrice.text = containerView.context.getString(R.string.price_tl, offer.price)

            binding.buttonSelect.setOnClickListener {
                listener?.offerSelected(offer)
            }

        }

    }

    interface OfferSelectListener {
        fun offerSelected(offer: FlexirideOffer)
    }

}