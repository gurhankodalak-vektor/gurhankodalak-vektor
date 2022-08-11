package com.vektortelekom.android.vservice.ui.home

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface HomeNavigator: BaseNavigator {

    fun showMenuActivity(view: View?)

    fun showPoolCarActivity(view: View?)

    fun showPoolCarReservationsActivity(view: View?)

    fun showPoolCarAddReservationActivity(isIntercity: Boolean)

    fun showStartTaxiActivity(view: View?)

    fun showReportTaxiActivity(view: View?)

    fun showTaxiListFragment(view: View?)

    fun showFlexirideActivity(type: Int)

    fun showFlexirideListActivity(view: View?)

    fun showCalendarActivity(view: View?)

    fun showPastUsesActivity(view: View?)

}