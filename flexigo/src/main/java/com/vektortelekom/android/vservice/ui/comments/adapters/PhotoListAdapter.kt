package com.vektortelekom.android.vservice.ui.comments.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.databinding.CommentsPhotoListAddViewHolderItemBinding
import com.vektortelekom.android.vservice.databinding.CommentsPhotoListViewHolderItemBinding
import com.vektortelekom.android.vservice.utils.GlideApp
import kotlinx.android.extensions.LayoutContainer

class PhotoListAdapter (private val photos: MutableList<String>, val listener: PhotoListener?) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 0) {
            val binding = CommentsPhotoListAddViewHolderItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            AddPhotoViewHolder(binding)
        }
        else {
            val binding = CommentsPhotoListViewHolderItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            PhotoListViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return photos.size+1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position == 0 && holder is AddPhotoViewHolder) {
            holder.bind()
        }
        else if(position != 0 && holder is PhotoListViewHolder) {
            holder.bind(photos[position-1])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0) {
            0
        }
        else {
            1
        }
    }

    inner class PhotoListViewHolder (val binding: CommentsPhotoListViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(photo: String) {
            GlideApp.with(containerView.context).load(photo).into(binding.imageViewPhoto)
        }

    }

    inner class AddPhotoViewHolder (val binding: CommentsPhotoListAddViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = TODO("Not yet implemented")

        fun bind() {
            //containerView is not used because of ripple effect of card view
            binding.cardViewAddPhoto.setOnClickListener {
                listener?.addPhotoClicked()
            }
        }

    }

    interface PhotoListener {
        fun addPhotoClicked()
    }

    fun addPhoto(photo: String) {
        photos.add(photo)
        notifyDataSetChanged()
    }

}