package com.vektortelekom.android.vservice.ui.route

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface RouteSelectionNavigator: BaseNavigator {
    fun backPressed(view: View?)

    fun showBottomSheetRoutePreview()

}