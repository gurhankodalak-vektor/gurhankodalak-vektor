package com.vektortelekom.android.vservice.ui.poolcar.reservation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.PersonnelModel
import com.vektortelekom.android.vservice.databinding.PoolCarPersonViewHolderBinding
import kotlinx.android.extensions.LayoutContainer

class PersonListAdapter(val people: MutableList<PersonnelModel>): RecyclerView.Adapter<PersonListAdapter.PersonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val binding = PoolCarPersonViewHolderBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return PersonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(people[position])
    }

    override fun getItemCount(): Int {
        return people.size
    }

    inner class PersonViewHolder (val binding: PoolCarPersonViewHolderBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(person: PersonnelModel) {

            binding.textViewPerson.text = person.identityNumber.plus(" ").plus(person.fullName)

            binding.butonDeleteUser.setOnClickListener {
                people.remove(person)
                notifyDataSetChanged()
            }

        }

    }

}