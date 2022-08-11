package com.vektortelekom.android.vservice.ui.login

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface LoginNavigator: BaseNavigator {

    fun showLoginFragment(view: View?)

    fun showForgotPasswordFragment(view: View?)

}