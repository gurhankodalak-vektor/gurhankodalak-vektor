package com.vektortelekom.android.vservice.ui.calendar

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface CalendarNavigator: BaseNavigator {

    fun backPressed(view: View?)

    fun showCalendarAccountsFragment(view: View?)

    fun showMenuActivity(view: View?)

}