package com.vektortelekom.android.vservice.ui.poolcar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektor.vshare_api_ktx.model.DamageModel
import com.vektortelekom.android.vservice.data.remote.AppApiHelper
import com.vektortelekom.android.vservice.databinding.PoolCarDamagePhotoViewHolderItemBinding
import com.vektortelekom.android.vservice.ui.dialog.ImageZoomDialog
import com.vektortelekom.android.vservice.utils.GlideApp
import kotlinx.android.extensions.LayoutContainer

class DamagePhotosAdapter(private val damage : DamageModel) : RecyclerView.Adapter<DamagePhotosAdapter.DamageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DamageViewHolder {
        val binding = PoolCarDamagePhotoViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return DamageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return damage.fileUuids?.size?:0
    }

    override fun onBindViewHolder(holder: DamageViewHolder, position: Int) {
        damage.fileUuids?.get(position)?.let { holder.bind(it) }
    }

    inner class DamageViewHolder (val binding: PoolCarDamagePhotoViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(damageUuid: String) {

            val url: String = AppApiHelper().baseUrl2
                    .plus("/")
                    .plus("report/fileViewer/uuid/")
                    .plus(damageUuid)

            GlideApp.with(containerView).load(url).into(binding.imageViewDamage)

            binding.imageViewDamage.setOnClickListener {
                val imageZoomDialog = ImageZoomDialog(containerView.context, AppApiHelper(), damageUuid, false)
                imageZoomDialog.show()
            }

        }

    }

}