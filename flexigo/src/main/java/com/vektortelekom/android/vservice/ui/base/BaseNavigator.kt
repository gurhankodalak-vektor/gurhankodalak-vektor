package com.vektortelekom.android.vservice.ui.base

import android.view.View

interface BaseNavigator {

    fun handleError(error: Throwable)
    fun onFragmentDetached(tag: String)
    fun moveNext(view: View?)
    fun movePrevious(view: View?)
    fun showLoginActivity()
    fun showHomeActivity()

}
