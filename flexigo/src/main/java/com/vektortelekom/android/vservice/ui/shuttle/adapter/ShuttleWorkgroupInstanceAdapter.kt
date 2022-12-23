package com.vektortelekom.android.vservice.ui.shuttle.adapter

import android.content.Context
import android.os.Build
import android.util.Log
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
import com.vektortelekom.android.vservice.utils.convertMinutesToDayText
import com.vektortelekom.android.vservice.utils.convertToShuttleDateTime
import com.vektortelekom.android.vservice.utils.convertToShuttleReservationTime
import kotlinx.android.extensions.LayoutContainer
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ShuttleWorkgroupInstanceAdapter(val listener: WorkGroupInstanceItemClickListener) : RecyclerView.Adapter<ShuttleWorkgroupInstanceAdapter.ShuttleWorkgroupInstanceViewHolderViewHolder>() {

    private var reservations: List<WorkGroupInstance> = listOf()
    private var workGroupSameNameList: List<WorkGroupInstance> = listOf()
    private var templates: List<WorkGroupTemplate> = listOf()
    private var destinations: List<DestinationModel> = listOf()

    var isSingleGroup = true

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
                isSingleGroup = false

                timeText =  containerView.context.getString(R.string.multi_hours)
            } ?: run {
                isSingleGroup = true
            }

            timeText.let {
                binding.textViewTime.text = timeText
            }

            binding.textviewWorkgroupName.text = template?.name

            val toText = when(template?.fromType){
                FromToType.PERSONNEL_SHUTTLE_STOP -> containerView.context.getString(R.string.stops)
                FromToType.PERSONNEL_HOME_ADDRESS -> containerView.context.getString(R.string.home_address)
                FromToType.CAMPUS ,
                FromToType.PERSONNEL_WORK_LOCATION -> {
                    getDestinationInfo(template)
                }

                else -> { "" }
            }

            val fromText = when(template?.toType){
                FromToType.PERSONNEL_SHUTTLE_STOP -> containerView.context.getString(R.string.stops)
                FromToType.PERSONNEL_HOME_ADDRESS -> containerView.context.getString(R.string.home_address)
                FromToType.CAMPUS ,
                FromToType.PERSONNEL_WORK_LOCATION -> {
                    getDestinationInfo(template)
                }

                else -> { "" }
            }

            binding.textviewWorkgroupFromTo.text = fromText.plus(" - ").plus(toText)

            if (model.workgroupStatus == WorkgroupStatus.PENDING_DEMAND && model.demandDeadline != null){

                binding.imageviewHourGlass.visibility = View.VISIBLE
                binding.textviewClosestTime.visibility = View.VISIBLE

                model.demandDeadline.convertToShuttleReservationTime()

                val deadLine = getDateDifference(model.demandDeadline, containerView.context)

                if (isSingleGroup)
                    binding.textviewClosestTime.text = containerView.context.getString(R.string.single_hour, deadLine)
                else
                    binding.textviewClosestTime.text = containerView.context.getString(R.string.multi_hour, deadLine)

            } else{

                binding.imageviewHourGlass.visibility = View.GONE
                binding.textviewClosestTime.visibility = View.GONE

            }

        }

    }

    private fun getDateDifference(demandDeadline: Long, context: Context): String{

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        val date1 = demandDeadline.convertToShuttleReservationTime()

        try {
            val deadline: Date = dateFormat.parse(date1) as Date

            val currentDate = Date()
            val diff = deadline.time - currentDate.time
            val minutes = diff / 1000 / 60


            return minutes.toInt().convertMinutesToDayText(context)

        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getDestinationInfo(template: WorkGroupTemplate) : String{

        var destinationInfo = ""

        destinations.let { destinations ->
            destinations.forEachIndexed { _, destinationModel ->
                if (template.fromType == FromToType.CAMPUS || template.fromType == FromToType.PERSONNEL_WORK_LOCATION){

                    if(destinationModel.id == template.fromTerminalReferenceId)
                        destinationInfo = destinationModel.title ?: ""

                } else{

                    if(destinationModel.id == template.toTerminalReferenceId)
                        destinationInfo = destinationModel.title ?: ""

                }

            }
        }

        if (destinationInfo == "")
            destinationInfo = destinations.first().title ?: ""

        return destinationInfo
    }

    fun setList(reservations: List<WorkGroupInstance>, templates: List<WorkGroupTemplate>, workGroupSameNameList: List<WorkGroupInstance>, destinations: List<DestinationModel>) {
        this.reservations = reservations
        this.templates = templates
        this.workGroupSameNameList = workGroupSameNameList
        this.destinations = destinations

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