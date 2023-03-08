package com.vektortelekom.android.vservice.ui.base.component.surveyview

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.vektortelekom.android.vservice.ui.base.component.cardtimerview.TimeWithStatusViewData

class SurveyItemViewModel : ViewModel() {

    var viewdata = ObservableField<SurveyItemViewData>()
    var surveyItemTextObservable = ObservableField<String>()
    var isChecked = false

    fun handleData(data: SurveyItemViewData?){
        data?.let {
            viewdata.set(it)
            surveyItemTextObservable.set(it.surveyText)
            isChecked = data.isChecked
        }
    }
}