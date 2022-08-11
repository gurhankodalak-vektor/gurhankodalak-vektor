package com.vektortelekom.android.vservice.ui.comments.adapters

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
import com.vektortelekom.android.vservice.data.model.TicketModel
import com.vektortelekom.android.vservice.data.model.TicketStatus
import com.vektortelekom.android.vservice.databinding.CommentsViewHolderItemBinding
import com.vektortelekom.android.vservice.utils.convertToTicketTime
import kotlinx.android.extensions.LayoutContainer
import java.util.*

class CommentListAdapter(val comments: List<TicketModel>) : RecyclerView.Adapter<CommentListAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CommentsViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    inner class CommentViewHolder (val binding: CommentsViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {

        override val containerView: View
            get() = TODO("Not yet implemented")

        fun bind(comment : TicketModel) {

            binding.textViewTitle.text = comment.title
            binding.textViewTime.text = Date(comment.creationTime).convertToTicketTime()
            binding.textViewMessage.text = comment.description
            binding.textViewStatus.text = comment.localizedTicketStatus

            when(comment.ticketStatus) {
                TicketStatus.OPEN -> {
                    binding.cardViewStatus.strokeColor = ContextCompat.getColor(binding.root.context, R.color.marigold)
                    binding.textViewStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.marigold))
                }
                TicketStatus.CLOSED -> {
                    binding.cardViewStatus.strokeColor = ContextCompat.getColor(binding.root.context, R.color.watermelon)
                    binding.textViewStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.watermelon))
                }
                else -> {
                    binding.cardViewStatus.strokeColor = ContextCompat.getColor(binding.root.context, R.color.marigold)
                    binding.textViewStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.marigold))
                }
            }

            if(comment.logs.isNullOrEmpty().not() && comment.logs?.get(0)?.creationTime != null && comment.logs[0].logDescription != null) {
                binding.textViewResponse.visibility = View.VISIBLE

                val sentBy = binding.root.context.getString(R.string.response)
                val messageText = comment.logs[0].logDescription

                val firstIndex = (sentBy.length) +1
                val lastIndex = firstIndex + (messageText?.length?:0) + 1
                val spanText =  SpannableStringBuilder()
                        .append(sentBy)
                        .append(": ")
                        .append(messageText)

                spanText.setSpan(TextAppearanceSpan(binding.root.context, R.style.TextMessageResponse),0, firstIndex-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spanText.setSpan(ForegroundColorSpan(ContextCompat.getColor(binding.root.context, R.color.darkNavyBlue)), firstIndex, lastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                binding.textViewResponse.text = spanText
            }
            else {
                binding.textViewResponse.visibility = View.GONE
            }

        }

    }

}