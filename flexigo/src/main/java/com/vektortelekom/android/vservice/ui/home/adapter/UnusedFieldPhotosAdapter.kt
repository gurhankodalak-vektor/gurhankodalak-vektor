package com.vektortelekom.android.vservice.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.databinding.HomeUnusedFieldPhotosViewHolderBinding
import kotlinx.android.extensions.LayoutContainer

class UnusedFieldPhotosAdapter: RecyclerView.Adapter<UnusedFieldPhotosAdapter.UnusedFieldsPhotoViewHolder>() {
    var photos: List<String>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnusedFieldsPhotoViewHolder {
        val binding = HomeUnusedFieldPhotosViewHolderBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return UnusedFieldsPhotoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return photos?.size?:0
    }

    override fun onBindViewHolder(holder: UnusedFieldsPhotoViewHolder, position: Int) {
        photos?.let {
            holder.bind(it[position])
        }
    }

    inner class UnusedFieldsPhotoViewHolder(val binding: HomeUnusedFieldPhotosViewHolderBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(photo: String) {
            binding.imageViewUnusedField.setImageResource(containerView.resources.getIdentifier(photo, "drawable", containerView.context.packageName))
        }

    }

    fun setPhotoList(photos: List<String>) {
        this.photos = photos
        notifyDataSetChanged()

    }

}