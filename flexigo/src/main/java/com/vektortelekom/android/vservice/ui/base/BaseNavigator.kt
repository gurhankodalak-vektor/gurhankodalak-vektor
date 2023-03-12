package com.vektortelekom.android.vservice.ui.base

import android.view.View
import com.vektortelekom.android.vservice.data.model.CheckDomainRequest
import com.vektortelekom.android.vservice.data.model.RegisterVerifyCompanyCodeRequest

interface BaseNavigator {

    fun handleError(error: Throwable)
    fun handleErrorMessage(error: String)
    fun onFragmentDetached(tag: String)
    fun moveNext(view: View?)
    fun movePrevious(view: View?)
    fun showLoginActivity()
    fun showHomeActivity()
    fun showRegisterActivity()
    fun tryLoginWithOtherServer(username: String, password: String, isFirstTry: Boolean)
    fun tryCheckDomainWithOtherServer(checkDomainRequest: CheckDomainRequest, langCode: String)
    fun tryCompanyCodeWithOtherServer(registerVerifyCompanyCodeRequest: RegisterVerifyCompanyCodeRequest, langCode: String)
    fun showLandingActivity()
}
