package com.vektortelekom.android.vservice.ui.carpool.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.CarpoolListItemBinding
import com.vektortelekom.android.vservice.ui.shuttle.adapter.ShuttleRegularRoutesAdapter
import kotlinx.android.extensions.LayoutContainer

class CarPoolMatchedAdapter(var pageName: String): RecyclerView.Adapter<CarPoolMatchedAdapter.ViewHolder>() {

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

            if (pageName == "drivers")
                binding.imageviewMatchRider.visibility = View.GONE

            if (pageName == "riders")
                binding.imageviewMatchRider.visibility = View.VISIBLE


            binding.textviewNameSurname.text = item.name.plus(" ").plus(item.surname)

//            containerView.rootView.setOnClickListener {
//                onClick(route)
//            }

        }
    }

    fun setList(list: List<CarPoolListModel>) {
        this.list = list
        notifyDataSetChanged()
    }


}