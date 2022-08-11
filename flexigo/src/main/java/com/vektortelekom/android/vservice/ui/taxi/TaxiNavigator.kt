package com.vektortelekom.android.vservice.ui.taxi

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface TaxiNavigator: BaseNavigator {

    fun backPressed(view: View?)

    fun showTaxiStartFragment(view: View?)

    fun showTaxiFinishFragment(view: View?)

    fun showTaxiReportFragment(view: View?)

    fun showTaxiListFragment(view: View?)

    fun showReportStartLocationFragment(view: View?)

    fun showReportEndLocationFragment(view: View?)

    fun showFinishEndLocationFragment(view: View?)

    fun showStartStartLocationFragment(view: View?)

    //fun showStartEndLocationFragment(view: View?)

    fun updateAddress(view: View?)

}