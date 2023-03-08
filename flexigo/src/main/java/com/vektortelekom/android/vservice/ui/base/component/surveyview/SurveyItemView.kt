package com.vektortelekom.android.vservice.ui.base.component.surveyview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.ViewSurveyBinding



class SurveyItemView  @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSurveyBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_survey,
            this,
            true
    )
    
}