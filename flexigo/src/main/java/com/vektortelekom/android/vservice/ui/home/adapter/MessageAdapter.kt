package com.vektortelekom.android.vservice.ui.home.adapter

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.MessageModel
import com.vektortelekom.android.vservice.databinding.HomeMessageViewHolderItemBinding
import kotlinx.android.extensions.LayoutContainer

class MessageAdapter (private val messages: List<MessageModel>): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = HomeMessageViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    inner class MessageViewHolder ( val binding: HomeMessageViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(message: MessageModel) {

            val sentBy = message.sentBy
            val messageText = message.message

            val firstIndex = (sentBy.length) +1
            val lastIndex = firstIndex + messageText.length + 1
            val spanText =  SpannableStringBuilder()
                    .append(sentBy)
                    .append(": ")
                    .append(messageText)

            spanText.setSpan(TextAppearanceSpan(containerView.context, R.style.TextMessageSender),0, firstIndex-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spanText.setSpan(ForegroundColorSpan(ContextCompat.getColor(containerView.context, R.color.darkNavyBlue)), firstIndex, lastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            binding.textViewMessage.text = spanText

        }

    }

}