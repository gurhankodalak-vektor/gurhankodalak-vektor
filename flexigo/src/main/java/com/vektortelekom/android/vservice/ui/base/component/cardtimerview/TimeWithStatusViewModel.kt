package com.vektortelekom.android.vservice.ui.base.component.cardtimerview

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

class TimeWithStatusViewModel : ViewModel() {

    var viewdata = ObservableField<TimeWithStatusViewData>()
    var timeTextObservable = ObservableField<String>()

    fun handleData(data: TimeWithStatusViewData?) {
        data?.let {
            viewdata.set(it)
            timeTextObservable.set(it.timeText)
        }
    }
}