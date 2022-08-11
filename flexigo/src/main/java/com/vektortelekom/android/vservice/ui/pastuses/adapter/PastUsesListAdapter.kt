package com.vektortelekom.android.vservice.ui.pastuses.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.PastUseModel
import com.vektortelekom.android.vservice.databinding.PastUsesViewHolderItemBinding

class PastUsesListAdapter(var pastUses: List<PastUseModel>): RecyclerView.Adapter<PastUsesListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
                DataBindingUtil.inflate<PastUsesViewHolderItemBinding>(
                        LayoutInflater.from(parent.context),
                        R.layout.past_uses_view_holder_item, parent, false
                )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pastUses[position])
    }

    override fun getItemCount(): Int {
        return pastUses.size
    }

    inner class ViewHolder(private val binding: PastUsesViewHolderItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(pastUse: PastUseModel) {

            binding.pastUseModel = pastUse
            binding.viewHolder = this

        }


    }

    fun setList(pastUses: List<PastUseModel>) {
        this.pastUses = pastUses
        notifyDataSetChanged()
    }

}