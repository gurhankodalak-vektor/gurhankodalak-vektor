package com.vektortelekom.android.vservice.ui.base.photo

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface TakePhotoNavigator: BaseNavigator {

    fun showViewPhotoFragment(view: View?)

    fun showTakePhotoFragment(view: View?)

}