package com.vektortelekom.android.vservice.ui.notification.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.NotificationModel
import com.vektortelekom.android.vservice.databinding.NotificationViewHolderItemBinding
import com.vektortelekom.android.vservice.utils.convertBackendDateToNotificationDate
import kotlinx.android.extensions.LayoutContainer

class NotificationsAdapter(private val notifications: List<NotificationModel>): RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    inner class NotificationViewHolder (val binding: NotificationViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(notification : NotificationModel) {
            binding.textViewMessage.text = notification.message
            binding.textViewDate.text = notification.creationTime.convertBackendDateToNotificationDate()

        }

    }

}