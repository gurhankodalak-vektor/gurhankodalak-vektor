package com.vektortelekom.android.vservice.ui.vanpool.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.PersonsModel
import com.vektortelekom.android.vservice.databinding.VanpoolPassengerBinding
import com.vektortelekom.android.vservice.databinding.VanpoolPassengerListItemBinding
import kotlinx.android.extensions.LayoutContainer

class VanpoolPassengerAdapter(val listener: VanpoolPassengerItemClickListener) : RecyclerView.Adapter<VanpoolPassengerAdapter.ViewHolder>() {

    private var passengerList: List<PersonsModel> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VanpoolPassengerAdapter.ViewHolder {
        val binding = VanpoolPassengerListItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return passengerList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(passengerList[position])
    }

    inner class ViewHolder(val binding: VanpoolPassengerListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {

        fun bind(model: PersonsModel) {
            binding.textviewPersonnelName.text = model.name.plus(" ").plus(model.surname)
            if (model.phoneNumber == null){
                binding.imageviewPhone.isEnabled = false
                binding.imageviewPhone.alpha = 0.3F
            }
            else
                binding.imageviewPhone.isEnabled = true

            binding.imageviewPhone.setOnClickListener {
                listener.onPersonsClick(model)
            }

        }

        override val containerView: View
            get() = TODO("Not yet implemented")
    }

    interface VanpoolPassengerItemClickListener {
        fun onPersonsClick(model: PersonsModel?)
    }

    fun setPassengerList(passengerList: List<PersonsModel>) {
        this.passengerList = passengerList
        notifyDataSetChanged()
    }

}