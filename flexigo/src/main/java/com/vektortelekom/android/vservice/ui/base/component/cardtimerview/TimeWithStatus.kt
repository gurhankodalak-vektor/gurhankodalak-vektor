package com.vektortelekom.android.vservice.ui.base.component.cardtimerview

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.WorkgroupStatus
import com.vektortelekom.android.vservice.databinding.ViewTimeWithStatusItemBinding

@BindingAdapter("item:setData")
fun TimeWithStatus.setData(data: TimeWithStatusViewData?) {
    timeWithStatusViewModel.handleData(data)
}

class TimeWithStatus constructor(context: Context) : FrameLayout(context) {

    var status: WorkgroupStatus? = null
    var isChecked: Boolean? = null
    var eventHandler: EventHandler? = null

    constructor(context: Context, status: WorkgroupStatus?, isChecked: Boolean?) : this(context) {
        this.status = status
        this.isChecked = isChecked
        initView()
    }

    interface EventHandler {
        fun onViewClick(view: TimeWithStatus?)
    }

    private val binding: ViewTimeWithStatusItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_time_with_status_item,
            this,
            true
    )

    val timeWithStatusViewModel = TimeWithStatusViewModel()

    init {
        binding.viewModel = timeWithStatusViewModel
        binding.card.setOnClickListener { eventHandler?.onViewClick(this) }
    }

    private fun initView() {
        when (status) {
            WorkgroupStatus.PENDING_DEMAND -> binding.statusView.background = AppCompatResources.getDrawable(context, R.drawable.ic_dotted_line)
            WorkgroupStatus.PENDING_PLANNING, WorkgroupStatus.PENDING_ASSIGNMENT -> binding.statusView.background = AppCompatResources.getDrawable(context, R.drawable.ic_dotted)
            WorkgroupStatus.ACTIVE -> binding.statusView.background = AppCompatResources.getDrawable(context, R.drawable.ic_line)
            else -> Unit
        }
        if (isChecked == true) {
            binding.card.strokeColor = ContextCompat.getColor(context, R.color.colorWeirdGreenTwo)
            binding.rootLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWeirdGreenTwo))
            binding.text.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
            binding.statusView.backgroundTintList = ContextCompat.getColorStateList(context, R.color.colorWhite)
        } else {
            binding.card.strokeColor = ContextCompat.getColor(context, R.color.paleGrey)
            binding.rootLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
            binding.text.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
            binding.statusView.backgroundTintList = ContextCompat.getColorStateList(context, R.color.steel)
        }
    }
}