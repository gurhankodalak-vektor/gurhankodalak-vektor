package com.vektortelekom.android.vservice.ui.poolcar.intercity

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface PoolCarIntercityNavigator: BaseNavigator {

    fun backPressed(view: View?)

    fun showMenuActivity(view: View?)

    fun showFinishFragment(view: View?)

    fun showRentalFragment()

    fun showStartFragment(view: View?)

    fun showVehicleRulesFragment(view: View?)

    fun showVehicleSpeedRulesFragment(view: View?)

}