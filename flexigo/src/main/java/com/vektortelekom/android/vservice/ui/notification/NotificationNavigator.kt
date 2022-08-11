package com.vektortelekom.android.vservice.ui.notification

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface NotificationNavigator : BaseNavigator {

    fun backPressed(view: View?)

    fun showMenuActivity(view: View?)

}