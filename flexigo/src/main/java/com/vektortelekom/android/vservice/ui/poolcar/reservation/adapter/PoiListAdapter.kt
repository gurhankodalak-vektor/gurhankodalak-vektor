package com.vektortelekom.android.vservice.ui.poolcar.reservation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.ParkModel
import com.vektortelekom.android.vservice.data.model.PoiModel
import com.vektortelekom.android.vservice.databinding.PoolCarSelectPoiViewHolderItemBinding
import kotlinx.android.extensions.LayoutContainer

class PoiListAdapter(var poiList: List<PoiListItem>, val onClick: (PoiListItem) -> Unit): RecyclerView.Adapter<PoiListAdapter.PoiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoiViewHolder {
        val binding = PoolCarSelectPoiViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return PoiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PoiViewHolder, position: Int) {
        holder.bind(poiList[position])
    }

    override fun getItemCount(): Int {
        return poiList.size
    }


    inner class PoiViewHolder(val binding: PoolCarSelectPoiViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(item: PoiListItem) {
            binding.textViewItem.text = if(item is PoiModel) item.name else if (item is ParkModel) item.name else ""

            containerView.setOnClickListener {
                onClick(item)
            }

        }

    }

    fun setList(poiList: List<PoiListItem>) {
        this.poiList = poiList
        notifyDataSetChanged()
    }

    interface PoiListItem {

    }

}