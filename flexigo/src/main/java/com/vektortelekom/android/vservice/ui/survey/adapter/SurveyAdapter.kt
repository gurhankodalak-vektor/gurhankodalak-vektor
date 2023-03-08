package com.vektortelekom.android.vservice.ui.survey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.ListItemSurveyBinding
import com.vektortelekom.android.vservice.databinding.ViewSurveyBinding
import com.vektortelekom.android.vservice.ui.base.component.surveyview.SurveyItemViewData

class SurveyAdapter(
        private var surveyList: List<SurveyItemViewData>,
        private val listener: SurveyItemListener
) : RecyclerView.Adapter<SurveyAdapter.SurveyItemViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyItemViewHolder {
        val binding = ListItemSurveyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SurveyItemViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: SurveyItemViewHolder, position: Int) {
        holder.bind(surveyList[position])
    }

    fun setList(list: List<SurveyItemViewData>) {
        surveyList = list
    }

    override fun getItemCount() = surveyList.size


    class SurveyItemViewHolder(
            private val binding: ListItemSurveyBinding,
            private val listener: SurveyItemListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SurveyItemViewData) {

            binding.surveyText.text = data.surveyText

            if (data.isChecked) {
                binding.surveyCard.setCardBackgroundColor(ContextCompat.getColorStateList(binding.root.context, R.color.corn_flower))
                binding.surveyText.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorWhite))
            } else {
                binding.surveyCard.setCardBackgroundColor(ContextCompat.getColorStateList(binding.root.context, R.color.colorWhite))
                binding.surveyText.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorBlack))
            }

            binding.root.setOnClickListener {
                listener.onClickItem(data)
            }
        }
    }

}