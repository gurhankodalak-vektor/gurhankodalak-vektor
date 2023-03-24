package com.vektortelekom.android.vservice.ui.shuttle

import android.view.View
import com.vektortelekom.android.vservice.data.model.ShuttleNextRide
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface ShuttleNavigator: BaseNavigator {

    fun showShuttleMainFragment()

    fun showInformationFragment()

    fun backPressed(view: View?)

    fun showRouteSearchFromFragment(view: View?)

    fun showRouteSearchToFragment(view: View?)

    fun showFromToMapFragment(view: View?)

    fun showMenuActivity(view: View?)

    fun changeTitle(title: String)

    fun highlightSearchRouteFinished()

    fun setRouteFilterVisibility(visibility: Boolean)

    fun showRouteFilter(view: View?)

    fun changeBottomNavigatorVisibility(isVisible: Boolean)

    fun changeToolBarVisibility(isVisible: Boolean)

    fun setToolBarText(text: String)

    fun showServicePlanningReservationFragment()

    fun startQrActivity(data: String?)

    fun showQrReadActivity()

    fun showAttendanceConfirmationDialog(ride: ShuttleNextRide)
}