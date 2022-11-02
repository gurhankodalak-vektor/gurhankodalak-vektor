package com.vektortelekom.android.vservice.ui.survey.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.CommuteOptionsModel
import com.vektortelekom.android.vservice.data.model.RouteModel
import com.vektortelekom.android.vservice.databinding.CommuteOptionsListItemBinding
import kotlinx.android.extensions.LayoutContainer

class CommuteOptionsAdapter(val listener: CommuteOptionsItemClickListener): RecyclerView.Adapter<CommuteOptionsAdapter.CommuteOptionsViewHolder>() {

    private var commuteOptionsList: List<CommuteOptionsModel> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommuteOptionsAdapter.CommuteOptionsViewHolder {
        val binding = CommuteOptionsListItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return CommuteOptionsViewHolder(binding)
    }

    fun setList(commuteOptionsList: List<CommuteOptionsModel>) {
        this.commuteOptionsList = commuteOptionsList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return commuteOptionsList.size
    }

    override fun onBindViewHolder(holder: CommuteOptionsViewHolder, position: Int) {
        holder.bind(commuteOptionsList[position])
    }

    inner class CommuteOptionsViewHolder (val binding: CommuteOptionsListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {

        override val containerView: View
            get() = binding.root

        fun bind(item: CommuteOptionsModel) {

            binding.textViewTitle.text = item.title
            binding.textViewSubtitle.text = item.subtitle

            binding.textViewCost.text = item.cost.toString()
            binding.textViewCostUnit.text = item.costUnit.format("%.2f")

            binding.textViewDuration.text = item.durationValue.toString()
            binding.textViewDurationUnit.text = item.durationUnit

            binding.textViewEmission.text = item.emissionValue.toString()
            binding.textViewEmissionUnit.text = item.emissionUnit

            if (item.optionsButtonVisibility == true)
                binding.buttonOptions.visibility = View.VISIBLE
            else
                binding.buttonOptions.visibility = View.GONE

            binding.buttonOptions.setOnClickListener {
                listener.onItemClicked()
            }

        }

    }
    interface CommuteOptionsItemClickListener {
        fun onItemClicked()
    }


}