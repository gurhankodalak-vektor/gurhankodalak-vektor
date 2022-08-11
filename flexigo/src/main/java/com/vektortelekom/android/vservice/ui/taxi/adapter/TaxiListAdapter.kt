package com.vektortelekom.android.vservice.ui.taxi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.TaxiUsage
import com.vektortelekom.android.vservice.databinding.TaxiListViewHolderItemBinding
import com.vektortelekom.android.vservice.utils.convertToTicketTime
import kotlinx.android.extensions.LayoutContainer
import java.util.*

class TaxiListAdapter(private val taxiUsages: List<TaxiUsage>) : RecyclerView.Adapter<TaxiListAdapter.TaxiUsageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxiUsageViewHolder {
        val binding = TaxiListViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return TaxiUsageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return taxiUsages.size
    }

    override fun onBindViewHolder(holder: TaxiUsageViewHolder, position: Int) {
        holder.bind(taxiUsages[position])
    }

    inner class TaxiUsageViewHolder (val binding: TaxiListViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(taxiUsage: TaxiUsage) {
            binding.textViewTitle.text = taxiUsage.explanation
            binding.textViewTime.text = Date(taxiUsage.creationTime).convertToTicketTime()
            binding.textViewMessage.text = taxiUsage.explanation
            binding.textViewStatus.text = taxiUsage.status
        }

    }

}