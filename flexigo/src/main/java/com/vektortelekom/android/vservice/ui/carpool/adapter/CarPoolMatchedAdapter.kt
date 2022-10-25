package com.vektortelekom.android.vservice.ui.carpool.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.CarpoolListItemBinding
import com.vektortelekom.android.vservice.ui.shuttle.adapter.ShuttleRegularRoutesAdapter
import com.vektortelekom.android.vservice.utils.convertHourMinutes
import kotlinx.android.extensions.LayoutContainer

class CarPoolMatchedAdapter(var pageName: String, val listener: CarPoolItemClickListener): RecyclerView.Adapter<CarPoolMatchedAdapter.ViewHolder>() {

    private var list: List<CarPoolListModel> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarPoolMatchedAdapter.ViewHolder {
        val binding = CarpoolListItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ViewHolder (val binding: CarpoolListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(item: CarPoolListModel) {

            binding.swipeLayout.isSwipeEnabled = false

            if (pageName == "drivers") {
                binding.imageviewNavigation.visibility = View.GONE
                binding.imageviewMatch.visibility = View.VISIBLE
            }

            if (pageName == "riders" || pageName == "riders_match") {
                binding.imageviewMatch.visibility = View.GONE
                binding.imageviewNavigation.visibility = View.GONE
            }

            if (pageName == "drivers_match") {
                binding.imageviewMatch.visibility = View.GONE
                binding.imageviewNavigation.visibility = View.VISIBLE
            }

            val time = item.arrivalHour.convertHourMinutes().plus(" - ").plus(item.departureHour.convertHourMinutes())

            binding.textviewNameSurname.text = item.name.plus(" ").plus(item.surname)
            binding.textviewDepartment.text = item.department ?: ""
            binding.textviewDepartureTime.text = time

            binding.imageviewCancel.setOnClickListener {
                listener.onCancelClicked(item)
            }
            binding.imageviewMatch.setOnClickListener {
                listener.onApproveClicked(item)
            }
            binding.imageviewCall.setOnClickListener {
                listener.onCallClicked(item)
            }

        }
    }

    interface CarPoolItemClickListener {
        fun onCancelClicked(item: CarPoolListModel)
        fun onApproveClicked(item: CarPoolListModel)
        fun onCallClicked(item: CarPoolListModel)
    }

    fun setList(list: List<CarPoolListModel>) {
        this.list = list
        notifyDataSetChanged()
    }


}