package com.vektortelekom.android.vservice.ui.shuttle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupInstance
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupTemplate
import com.vektortelekom.android.vservice.databinding.WorkgroupListItemBinding
import com.vektortelekom.android.vservice.utils.convertHourMinutes
import com.vektortelekom.android.vservice.utils.convertToShuttleDateTime
import kotlinx.android.extensions.LayoutContainer

class ShuttleWorkgroupInstanceAdapter(val listener: WorkGroupInstanceItemClickListener) : RecyclerView.Adapter<ShuttleWorkgroupInstanceAdapter.ShuttleWorkgroupInstanceViewHolderViewHolder>() {

    private var reservations: List<WorkGroupInstance> = listOf()
    private var workGroupSameNameList: List<WorkGroupInstance> = listOf()
    private var templates: List<WorkGroupTemplate> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShuttleWorkgroupInstanceAdapter.ShuttleWorkgroupInstanceViewHolderViewHolder {
        val binding = WorkgroupListItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ShuttleWorkgroupInstanceViewHolderViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reservations.size
    }

    override fun onBindViewHolder(holder: ShuttleWorkgroupInstanceViewHolderViewHolder, position: Int) {
        holder.bind(reservations[position])
    }

    inner class ShuttleWorkgroupInstanceViewHolderViewHolder(val binding: WorkgroupListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(model: WorkGroupInstance) {

            containerView.setOnClickListener {
                listener.onItemClicked(model)
            }
            val template = getTemplateForInstance(model)

            var timeText = model.firstDepartureDate.convertToShuttleDateTime()

            if (template?.direction == WorkgroupDirection.ROUND_TRIP) {
                val firstDeparture = template.shift?.departureHour.convertHourMinutes() ?: template.shift?.arrivalHour.convertHourMinutes()
                val returnDeparture = template.shift?.returnDepartureHour.convertHourMinutes() ?: template.shift?.returnArrivalHour.convertHourMinutes()
                timeText = firstDeparture.plus("-").plus(returnDeparture)
            }
            else {
                val firstDeparture = template?.shift?.departureHour.convertHourMinutes() ?: template?.shift?.arrivalHour.convertHourMinutes()
                if (firstDeparture != null) {
                    timeText = firstDeparture
                }
            }

            workGroupSameNameList.find {
                it.name == model.name }?.let {
                timeText =  containerView.context.getString(R.string.multi_hours)
            } ?: run {
            }

            model.name.let {
                if (it!!.contains("["))
                    binding.textViewWorkgroupInstanceName.text = it.split("[")[0]
                else
                    binding.textViewWorkgroupInstanceName.text = it

            }
            timeText.let {
                binding.textViewTime.text = timeText
            }
        }

    }

    fun setList(reservations: List<WorkGroupInstance>, templates: List<WorkGroupTemplate>, workGroupSameNameList: List<WorkGroupInstance>) {
        this.reservations = reservations
        this.templates = templates
        this.workGroupSameNameList = workGroupSameNameList
        notifyDataSetChanged()
    }

    interface WorkGroupInstanceItemClickListener {
        fun onItemClicked(workgroupInstance: WorkGroupInstance)
    }

    private fun getTemplateForInstance(workgroupInstance: WorkGroupInstance): WorkGroupTemplate? {
        templates.let { templates ->
            for (template in templates) {
                if (template.id == workgroupInstance.templateId) {
                    return  template
                }
            }
        }

        return null
    }

}