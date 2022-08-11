package com.vektortelekom.android.vservice.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.NotificationModel
import com.vektortelekom.android.vservice.databinding.HomeNotificationViewHolderItemBinding
import kotlinx.android.extensions.LayoutContainer

class NotificationsAdapter(private var notifications: List<NotificationModel>, val listener: NotificationListener?): RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = HomeNotificationViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    inner class NotificationViewHolder (val binding: HomeNotificationViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(notification: NotificationModel) {

            binding.textViewNotification.text = notification.message

            binding.cardViewNotification.setOnClickListener {
                listener?.notificationClicked()
            }

        }

    }

    fun updateNotifications(notifications: List<NotificationModel>) {
        this.notifications = notifications
        notifyDataSetChanged()
    }

    interface NotificationListener {
        fun notificationClicked()
    }

}