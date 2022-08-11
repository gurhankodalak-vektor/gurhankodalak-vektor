package com.vektortelekom.android.vservice.ui.poolcar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektor.vshare_api_ktx.model.DamageModel
import com.vektortelekom.android.vservice.data.remote.AppApiHelper
import com.vektortelekom.android.vservice.databinding.PoolCarAddDamagePhotoViewHolderItemBinding
import com.vektortelekom.android.vservice.databinding.PoolCarDamagePhotoViewHolderItemBinding
import com.vektortelekom.android.vservice.ui.dialog.ImageZoomDialog
import com.vektortelekom.android.vservice.utils.GlideApp
import kotlinx.android.extensions.LayoutContainer

class AddDamagePhotosAdapter(private var damage : DamageModel, private val listener: DamageListener?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if(viewType == 0) {
            val binding = PoolCarDamagePhotoViewHolderItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            DamageViewHolder(binding)
        }
        else {
            val binding = PoolCarAddDamagePhotoViewHolderItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            AddDamageViewHolder(binding)
        }

    }

    override fun getItemCount(): Int {
        return (damage.fileUuids?.size?:0) + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == (damage.fileUuids?.size ?: 0)) {
            1
        }
        else {
            0
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is DamageViewHolder) {
            damage.fileUuids?.get(position)?.let { holder.bind(it) }
        }
        else if(holder is AddDamageViewHolder) {
            holder.bind()
        }
    }

    inner class DamageViewHolder (val binding: PoolCarDamagePhotoViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(damageUuid: String) {
            GlideApp.with(containerView).load(damageUuid).into(binding.imageViewDamage)

            binding.cardViewPhoto.setOnClickListener {
                val imageZoomDialog = ImageZoomDialog(containerView.context, AppApiHelper(), damageUuid, true)
                imageZoomDialog.show()
            }

        }

    }

    inner class AddDamageViewHolder (val binding: PoolCarAddDamagePhotoViewHolderItemBinding): RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind() {
            binding.cardViewAddPhoto.setOnClickListener {
                listener?.addDamage()
            }

        }
    }

    interface DamageListener {
        fun addDamage()
    }

    fun changeDamage(damage: DamageModel) {
        this.damage = damage
        notifyDataSetChanged()
    }

}