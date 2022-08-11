package com.vektortelekom.android.vservice.ui.poolcar

import android.view.View
import com.vektor.vshare_api_ktx.model.DamageModel
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface PoolCarNavigator: BaseNavigator {

    fun backPressed(view: View?)

    fun showPoolCarMainFragment(view: View?)

    fun showPoolCarParkFragment(view: View?)

    fun showPoolCarParkInMap(view: View?)

    fun showPoolCarVehicleInMap(view: View?)

    fun showPoolCarVehicleFragment(view: View?)

    fun showFindCarFragment(view: View?)

    fun showDirectorRentalStart(view: View?)

    fun cancelRental(view: View?)

    fun vehicleNotInLocation(view: View?)

    fun showExternalDamageControlFragment(view: View?)

    fun showInternalDamageControlFragment(view: View?)

    fun showRentalFragment(view: View?)

    fun callCallCenter(view: View?)

    fun showPoolCarAssistanceFragment(view: View?)

    fun showPoolCarAddNewDamageFragment(view: View?)

    fun showPoolCarAddNewInternalDamageFragment(view: View?)

    fun showPoolCarRentalFinishControlFragment(view: View?)

    fun showPoolCarDirectorUsageFinishFragment(view: View?)

    fun showPoolCarRentalFinishParkPhotoPreviewFragment(view: View?)

    fun showPoolCarAddNewDamagePreviewFragment(view: View?)

    fun showPoolCarAddNewInternalDamagePreviewFragment(view: View?)

    fun closeRentalFinishParkPhotoPreviewFragment(view: View?)

    fun useRentalFinishParkTakenPhoto(view: View?)

    fun useAddNewDamageTakenPhoto(view: View?)

    fun takeRentalFinishParkPhotoAgain(view: View?)

    fun takeAddNewDamagePhotoAgain(view: View?)

    fun showPoolCarRentalFinishParkInfoFragment(view: View?)

    fun damageAdded(damage: DamageModel, shouldClose: Boolean)

    fun showPoolCarSatisfactionSurveyFragment()

    fun rentalFinished()

    fun showNotClosedDoorDialog()

    fun showNotOpenedDoorDialog()

    fun startRental(view: View?)

    fun finishRental(view: View?)

    fun qrReaderClose(view: View?)

    fun showUnauthorizedMessage()

    fun internalDamagesCompleted(view: View?)

    fun showMenuActivity(view: View?)

    fun showVehicleRulesFragment(view: View?)

    fun showVehicleSpeedRulesFragment(view: View?)

}