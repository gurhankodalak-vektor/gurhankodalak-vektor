package com.vektortelekom.android.vservice.ui.menu

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface MenuNavigator : BaseNavigator {

    fun showMenuMainFragment(view: View?)

    fun backPressed(view: View?)

    fun showMenuEditProfileFragment(view: View?)

    fun showMenuChangePasswordFragment(view: View?)

    fun showMenuCompanyInfoFragment(view: View?)

    fun showMenuAddressesFragment(view: View?)

    fun showMenuSettingsFragment(view: View?)

    fun showMenuPaymentFragment(view: View?)

    fun showMenuSignOutFragment(view: View?)

    fun returnMenuMainFragment()

    fun showPrivacyPolicyPdf(view: View?)

    fun showSecurityPdf(view: View?)

    fun showFlexigoPdf(view: View?)

    fun showAboutApplicationPdf(view: View?)

    fun takePhoto(view: View?)

    fun closePhotoPreviewFragment(view: View?)

    fun useTakenPhoto(view: View?)

    fun takePhotoAgain(view: View?)

    fun profilePhotoUpdated(photoUuid: String)

    fun showNotifications(view: View?)

    fun showMenuHighlightDialog(view: View?)

    fun showMenuQuestionnaireDialog(view: View?)

    fun showMenuDrivingLicenseDialog(view: View?)

    fun showMenuDrivingLicensePreviewFragment(view: View?)

    fun showDatePickerDialog(view: View?)

    fun showForgotPasswordFragment(view: View?)

    fun showBottomSheetCommuteOptions(view: View?)

    fun showRouteSelectionFragment(view: View?)

}