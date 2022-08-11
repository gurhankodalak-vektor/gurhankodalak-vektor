package com.vektortelekom.android.vservice.ui.flexiride

import android.view.View
import com.vektortelekom.android.vservice.data.model.FlexirideCreateResponseModel
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface FlexirideNavigator: BaseNavigator {

    fun backPressed(view: View?)

    fun showFlexirideFromFragment(view: View?)

    fun showFlexirideSearchFromFragment(view: View?)

    fun showFlexirideSearchToFragment(view: View?)

    fun showFlexirideListFragment(view: View?)

    fun confirmAddress(view: View?)

    fun showFlexiridePlannedFragment(view: View?)

    fun showFlexirideSurveyFragment()

    fun finishSurvey(view: View?)

    fun showUnauthorizedMessage()

}