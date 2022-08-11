package com.vektortelekom.android.vservice.ui.poolcar.reservation

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface PoolCarReservationNavigator: BaseNavigator {

    fun backPressed(view: View?)

    fun showPoolCarReservationsFragment(view: View?)

    fun showPoolCarAddReservationFragment(view: View?)

    fun backToHome(view: View?)

    fun showUnauthorizedMessage()

    fun showSelectToFragment(view: View?)

    fun updateTo(view: View?)

    fun showSelectPoiFragment()

    fun showQrFragment()
}