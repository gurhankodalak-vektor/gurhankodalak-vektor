package com.vektortelekom.android.vservice.ui.survey

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface SurveyNavigator: BaseNavigator {
    fun backPressed(view: View?)

    fun reloadFragment()

    fun showSurveyFragment()

    fun showMenuAddressesFragment()

    fun showVanPoolLocationPermissionFragment()

    fun showSurveyThankYouFragment()

}