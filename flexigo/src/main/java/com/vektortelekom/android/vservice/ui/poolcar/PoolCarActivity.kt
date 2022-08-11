package com.vektortelekom.android.vservice.ui.poolcar

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektor.vshare_api_ktx.model.DamageModel
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.PoolCarActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.menu.MenuActivity
import com.vektortelekom.android.vservice.ui.menu.fragment.MenuPdfViewerFragment
import com.vektortelekom.android.vservice.ui.poolcar.dialog.OdometerDialog
import com.vektortelekom.android.vservice.ui.poolcar.fragment.*
import java.lang.Exception
import javax.inject.Inject

class PoolCarActivity : BaseActivity<PoolCarViewModel>(),
        PoolCarNavigator,
        PermissionsUtils.LocationStateListener,
        PermissionsUtils.CameraStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    private lateinit var binding: PoolCarActivityBinding

    @Volatile
    private var myLocation: Location? = null
    private var locationClient: FusedLocationClient? = null
    private var reservation: PoolcarAndFlexirideModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<PoolCarActivityBinding>(this, R.layout.pool_car_activity).apply {
            lifecycleOwner = this@PoolCarActivity
            viewModel = this@PoolCarActivity.viewModel
        }
        viewModel.navigator = this


        viewModel.cancelRentalResponse.observe(this) {

            val mainFragment = supportFragmentManager.findFragmentByTag(PoolCarMainFragment.TAG)

            val transaction = supportFragmentManager.beginTransaction()

            for (fragment in supportFragmentManager.fragments.reversed()) {
                if (fragment != mainFragment) {
                    transaction.remove(fragment)
                }
            }
            transaction.commit()

            if (mainFragment == null) {
                showPoolCarMainFragment(null)
            }

        }


        if(intent.hasExtra("reservation")) {
            reservation = intent.getParcelableExtra("reservation") as? PoolcarAndFlexirideModel

            viewModel.selectedVehicle.value = reservation?.vehicle
            viewModel.selectedStation.value = reservation?.park



            viewModel.getCustomerStatusForReservation()

            viewModel.customerStatusReservation.observe(this, Observer {
                if (reservation?.flexirideRequest?.workflowType == ReservationWorkFlowType.LIGHT) {
                    viewModel.isStartAgreementApproved.value = true
                    showDirectorRentalStart(null)
                    return@Observer
                }
            })

            showPoolCarVehicleFragment(null)

            if (checkAndRequestLocationPermission(this)) {
                onLocationPermissionOk()
            }

            viewModel.isReservation = true
            viewModel.reservationId = reservation?.id

            return
        }

        viewModel.getCustomerStatus()

        viewModel.getMobileParameters()

        if (checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        }

        viewModel.customerStatus.observe(this, Observer { customerStatus ->
            if (reservation?.flexirideRequest?.workflowType == ReservationWorkFlowType.LIGHT) {
                showDirectorRentalStart(null)
                return@Observer
            }

            if(customerStatus.rental == null) {
                showPoolCarMainFragment(null)
            }
            else {
                when(customerStatus.rental.step) {
                    RentalStatus.WAITING_RENTAL_START -> {
                        showFindCarFragment(null)
                    }
                    RentalStatus.RENTAL_IN_PROGRESS -> {
                        showRentalFragment(null)
                    }
                    else -> {
                        showFindCarFragment(null)
                    }
                }
            }

        })

       viewModel.startRentalResponse.observe(this) {

       }

    }

    override fun showPoolCarMainFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarMainFragment.newInstance(), PoolCarMainFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showPoolCarParkFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarParkFragment.newInstance(), PoolCarParkFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showPoolCarVehicleFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarVehicleFragment.newInstance(), PoolCarVehicleFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showFindCarFragment(view: View?) {
        removeAllFragments()

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarFindCarFragment.newInstance(), PoolCarFindCarFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showDirectorRentalStart(view: View?) {
        removeAllFragments()
        supportFragmentManager
            .beginTransaction().add(R.id.root_fragment, PoolCarDirectorRentalStartRentalFragment.newInstance(), PoolCarDirectorRentalStartRentalFragment.TAG)
            .addToBackStack(null)
            .commit()
    }

    override fun cancelRental(view: View?) {
        AppDialog.Builder(this)
                .setIconVisibility(false)
                .setTitle(getString(R.string.cancel_rental))
                .setSubtitle(getString(R.string.cancel_rental_text))
                .setOkButton(getString(R.string.cancel)) { dialog ->
                    dialog.dismiss()
                    viewModel.cancelRental()
                }
                .create()
                .show()
    }

    override fun vehicleNotInLocation(view: View?) {
        AppDialog.Builder(this)
                .setIconVisibility(true)
                .setIcon(R.drawable.ic_attention)
                .setTitle(getString(R.string.cant_find_vehicle))
                .setSubtitle(getString(R.string.contact_call_centre))
                .setOkButton(getString(R.string.call_2)) { dialog ->
                    dialog.dismiss()
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus("")))
                    startActivity(intent)
                }
                .create()
                .show()
    }

    override fun showExternalDamageControlFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarExternalDamageControlFragment.newInstance(), PoolCarExternalDamageControlFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showInternalDamageControlFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarInternalDamageControlFragment.newInstance(), PoolCarInternalDamageControlFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showRentalFragment(view: View?) {

        removeAllFragments()

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarRentalFragment.newInstance(), PoolCarRentalFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun callCallCenter(view: View?) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(/*getString(R.string.call_center_number)*/"")))
        startActivity(intent)
    }

    override fun showPoolCarAssistanceFragment(view: View?) {
        /*supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarAssistanceFragment.newInstance(), PoolCarAssistanceFragment.TAG)
                .addToBackStack(null)
                .commit()*/

        FlexigoInfoDialog.Builder(this)
                .setIconVisibility(false)
                .setCancelable(true)
                .setTitle(getString(R.string.assistance))
                .setText1(getString(R.string.assistance_info_text))
                .setOkButton(getString(R.string.Generic_Ok)) {
                    it.dismiss()
                }
                .create()
                .show()
    }

    override fun showPoolCarAddNewDamageFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarAddNewDamageFragment.newInstance(), PoolCarAddNewDamageFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showPoolCarAddNewInternalDamageFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarAddNewInternalDamageFragment.newInstance(), PoolCarAddNewInternalDamageFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showPoolCarDirectorUsageFinishFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarFinishDirectorRentalFragment.newInstance(), PoolCarFinishDirectorRentalFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showPoolCarRentalFinishControlFragment(view: View?) {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.root_fragment, PoolCarRentalFinishControlFragment.newInstance(), PoolCarRentalFinishControlFragment.TAG)
            .addToBackStack(null)
            .commit()
    }

    override fun showPoolCarRentalFinishParkPhotoPreviewFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarRentalFinishParkPhotoPreviewFragment.newInstance(), PoolCarRentalFinishParkPhotoPreviewFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showPoolCarAddNewDamagePreviewFragment(view: View?) {

        viewModel.isPreviewExternal = true

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarAddNewDamagePreviewFragment.newInstance(), PoolCarAddNewDamagePreviewFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showPoolCarAddNewInternalDamagePreviewFragment(view: View?) {

        viewModel.isPreviewExternal = false

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarAddNewDamagePreviewFragment.newInstance(), PoolCarAddNewDamagePreviewFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun closeRentalFinishParkPhotoPreviewFragment(view: View?) {
        binding.layoutToolbar.visibility = View.VISIBLE
        onBackPressed()
    }

    override fun useRentalFinishParkTakenPhoto(view: View?) {
        binding.layoutToolbar.visibility = View.VISIBLE
        if(supportFragmentManager.findFragmentByTag(PoolCarRentalFinishParkPhotoPreviewFragment.TAG) != null) {
            onBackPressed()
        }
        val fragment = supportFragmentManager.findFragmentByTag(PoolCarRentalFinishParkInfoFragment.TAG)
        if(fragment is PoolCarRentalFinishParkInfoFragment) {
            fragment.addNewPhoto()
        }
    }

    override fun useAddNewDamageTakenPhoto(view: View?) {
        binding.layoutToolbar.visibility = View.VISIBLE
        if(supportFragmentManager.findFragmentByTag(PoolCarAddNewDamagePreviewFragment.TAG) != null) {
            onBackPressed()
        }
        val fragment = supportFragmentManager.findFragmentByTag(PoolCarAddNewDamageFragment.TAG)
        if(fragment is PoolCarAddNewDamageFragment) {
            fragment.addNewPhoto()
        }
        val fragment2 = supportFragmentManager.findFragmentByTag(PoolCarAddNewInternalDamageFragment.TAG)
        if(fragment2 is PoolCarAddNewInternalDamageFragment) {
            fragment2.addNewPhoto()
        }
    }

    override fun takeRentalFinishParkPhotoAgain(view: View?) {
        if(supportFragmentManager.findFragmentByTag(PoolCarRentalFinishParkPhotoPreviewFragment.TAG) != null) {
            onBackPressed()
        }
        val fragment = supportFragmentManager.findFragmentByTag(PoolCarRentalFinishParkInfoFragment.TAG)
        if(fragment is PoolCarRentalFinishParkInfoFragment) {
            fragment.takePhotoAgain()
        }
    }

    override fun takeAddNewDamagePhotoAgain(view: View?) {
        if(supportFragmentManager.findFragmentByTag(PoolCarAddNewDamagePreviewFragment.TAG) != null) {
            onBackPressed()
        }
        val fragment = supportFragmentManager.findFragmentByTag(PoolCarAddNewDamageFragment.TAG)
        if(fragment is PoolCarAddNewDamageFragment) {
            fragment.takePhotoAgain()
        }
        val fragment2 = supportFragmentManager.findFragmentByTag(PoolCarAddNewInternalDamageFragment.TAG)
        if(fragment2 is PoolCarAddNewInternalDamageFragment) {
            fragment2.takePhotoAgain()
        }
    }

    override fun showPoolCarRentalFinishParkInfoFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarRentalFinishParkInfoFragment.newInstance(), PoolCarRentalFinishParkInfoFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun damageAdded(damage: DamageModel, shouldClose: Boolean) {
        if(shouldClose
                && (supportFragmentManager.findFragmentByTag(PoolCarAddNewDamageFragment.TAG) != null
                        || supportFragmentManager.findFragmentByTag(PoolCarAddNewInternalDamageFragment.TAG) != null)) {
            onBackPressed()
        }
        val fragment = supportFragmentManager.findFragmentByTag(PoolCarExternalDamageControlFragment.TAG)
        if(fragment != null && fragment is PoolCarExternalDamageControlFragment) {
            fragment.damageAdded(damage)
        }
        val fragment2 = supportFragmentManager.findFragmentByTag(PoolCarInternalDamageControlFragment.TAG)
        if(fragment2 != null && fragment2 is PoolCarInternalDamageControlFragment) {
            fragment2.damageAdded(damage)
        }
    }

    override fun internalDamagesCompleted(view: View?) {
        if(supportFragmentManager.findFragmentByTag(PoolCarInternalDamageControlFragment.TAG) != null) {
            onBackPressed()
        }
    }

    override fun showMenuActivity(view: View?) {
        startActivity(Intent(this, MenuActivity::class.java))
    }

    override fun showPoolCarSatisfactionSurveyFragment() {
        removeAllFragments()

        binding.imageViewBack.setImageResource(R.drawable.ic_close)

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarSatisfactionSurveyFragment.newInstance(), PoolCarSatisfactionSurveyFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun rentalFinished() {
        finish()
    }

    override fun showNotClosedDoorDialog() {
        showErrorDoorDialog(getString(R.string.door_error_close))
    }

    override fun showNotOpenedDoorDialog() {
        showErrorDoorDialog(getString(R.string.door_error_open))
    }

    override fun startRental(view: View?) {

        val deviceType = viewModel.selectedVehicle.value?.deviceType

        if(deviceType == null) {
            handleError(Exception(getString(R.string.error_vehicle_info)))
            return
        }

        if(deviceType == DeviceType.NONE) {
            OdometerDialog(this, object: OdometerDialog.OdometerListener {
                override fun submitOdometer(value: Double) {
                    viewModel.startRentalOdometer = value
                    viewModel.startRental(this@PoolCarActivity)
                }

            }, true).show()
        }
        else {
            checkDistanceForStartFinishRental(true)
        }

    }

    override fun finishRental(view: View?) {

        val deviceType = viewModel.selectedVehicle.value?.deviceType

        if(deviceType == null) {
            handleError(Exception(getString(R.string.error_vehicle_info)))
            return
        }

        if(deviceType == DeviceType.NONE) {
            OdometerDialog(this, object: OdometerDialog.OdometerListener {
                override fun submitOdometer(value: Double) {
                    viewModel.endRentalOdometer = value
                    viewModel.finishRental(this@PoolCarActivity)
                }

            }, false).show()
        }
        else {
            checkDistanceForStartFinishRental(false)
        }
    }

    override fun showPoolCarParkInMap(view: View?) {

        val park = viewModel.selectedStation.value

        park?.let {
            navigateToMap(myLocation?.latitude?:0.0, myLocation?.longitude?:0.0, it.latitude?:0.0, it.longitude?:0.0)
        }
    }

    override fun showPoolCarVehicleInMap(view: View?) {

        val vehicle = viewModel.selectedVehicle.value

        vehicle?.location?.let {
            navigateToMap(myLocation?.latitude?:0.0, myLocation?.longitude?:0.0, it.latitude, it.longitude)
        }
    }

    override fun showVehicleRulesFragment(view: View?) {

        binding.textViewToolBar.text = getString(R.string.vehicle_rules_form)

        val fragment = MenuPdfViewerFragment.newInstance()
        val bundle = Bundle()
        bundle.putString("url", AppDataManager.instance.personnelInfo?.company?.rulesDocUrl)
        fragment.arguments = bundle

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, fragment, MenuPdfViewerFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showVehicleSpeedRulesFragment(view: View?) {

        binding.textViewToolBar.text = getString(R.string.vehicle_speed_rules_form)

        val fragment = MenuPdfViewerFragment.newInstance()
        val bundle = Bundle()
        bundle.putString("url", AppDataManager.instance.personnelInfo?.company?.securityDocUrl)
        fragment.arguments = bundle

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, fragment, MenuPdfViewerFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun qrReaderClose(view: View?) {
        supportFragmentManager.popBackStack()
    }

    override fun showUnauthorizedMessage() {
        FlexigoInfoDialog.Builder(this)
                .setIconVisibility(false)
                .setCancelable(false)
                .setTitle(getString(R.string.pool_car_unauthorized_title))
                .setText1(getString(R.string.pool_car_unauthorized_text))
                .setOkButton(getString(R.string.Generic_Ok)) {
                    finish()
                }
                .create()
                .show()
    }

    override fun getViewModel(): PoolCarViewModel {
        viewModel = ViewModelProvider(this, factory)[PoolCarViewModel::class.java]
        return viewModel
    }

    override fun backPressed(view: View?) {
        onBackPressed()
    }

    override fun onBackPressed() {

        val glideFragment = supportFragmentManager.findFragmentByTag("com.bumptech.glide.manager")

        val count = if (glideFragment == null) 1 else 2

        if (supportFragmentManager.fragments.size <= count) {
            finish()
        }
        else if(supportFragmentManager.findFragmentByTag(MenuPdfViewerFragment.TAG) != null) {
            binding.textViewToolBar.text = getString(R.string.pool_car)
            super.onBackPressed()
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onLocationPermissionOk() {

        locationClient = FusedLocationClient(this)

        locationClient?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient?.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            override fun onLocationUpdated(location: Location) {

                myLocation = location
                AppDataManager.instance.currentLocation = location
                viewModel.myLocation = location

            }

            override fun onLocationFailed(message: String) {
                if(this@PoolCarActivity.isFinishing || this@PoolCarActivity.isDestroyed) {
                    return
                }

                when (message) {
                    FusedLocationClient.ERROR_LOCATION_DISABLED -> locationClient?.showLocationSettingsDialog()
                    FusedLocationClient.ERROR_LOCATION_MODE -> {
                        locationClient?.showLocationSettingsDialog()
                    }
                    FusedLocationClient.ERROR_TIMEOUT_OCCURRED -> {
                        handleError(RuntimeException(getString(R.string.location_timeout)))
                    }
                }
            }

        })
    }

    override fun onLocationPermissionFailed() {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

    private fun removeAllFragments() {
        while(supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        }

        val transaction = supportFragmentManager.beginTransaction()

        for(fragment in supportFragmentManager.fragments) {
            transaction.remove(fragment)
        }
        transaction.commit()
    }

    private var isStart: Boolean? = null

    private var isCameraForQr = false

    private fun showStartQrDialog(isStart: Boolean) {
        FlexigoInfoDialog.Builder(this)
                .setIcon(R.drawable.ic_qr_code_dark_navy_blue)
                .setTitle(getString(R.string.rental_start_qr_title))
                .setText1(getString(R.string.rental_start_qr_text))
                .setCancelable(true)
                .setOkButton(getString(R.string.rental_start_qr_ok_button)) { dialog ->
                    dialog.dismiss()
                    this.isStart = isStart
                    isCameraForQr = true
                    if(checkAndRequestCameraPermission(this@PoolCarActivity)) {
                        onCameraPermissionOk()
                    }
                }.create().show()
    }

    override fun onCameraPermissionOk() {

        if(isCameraForQr) {
            val fragment = PoolCarRentalQrCodeReaderFragment.newInstance()
            val bundle = Bundle()
            bundle.putBoolean("isStart", isStart?:true)
            fragment.arguments = bundle

            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.root_fragment, fragment, PoolCarRentalQrCodeReaderFragment.TAG)
                    .addToBackStack(null)
                    .commit()
            isCameraForQr = false
        }

    }

    override fun onCameraPermissionFailed() {
        isCameraForQr = false
    }

    private fun checkDistanceForStartFinishRental(isStart: Boolean) {
        val radius = AppDataManager.instance.mobileParameters.vehicleDoorCommandRadius

        if(radius == null || radius == 0.0) {
            if(isStart) {
                viewModel.startRental(this)
            }
            else {
                viewModel.finishRental(this)
            }
        }
        else {
            val vehicleLocation = viewModel.selectedVehicle.value?.location
            if(vehicleLocation == null) {
                showStartQrDialog(isStart)
            }
            else {
                if(myLocation == null) {
                    showStartQrDialog(isStart)
                }
                else {
                    val vehicleLoc = Location("")
                    vehicleLoc.latitude = vehicleLocation.latitude
                    vehicleLoc.longitude = vehicleLocation.longitude
                    if(myLocation!!.distanceTo(vehicleLoc) > radius) {
                        showStartQrDialog(isStart)
                    }
                    else {
                        if(isStart) {
                            viewModel.startRental(this)
                        }
                        else {
                            viewModel.finishRental(this)
                        }
                    }
                }
            }
        }
    }

    fun getSelectedDeviceType(): DeviceType? {
        return viewModel.selectedVehicle.value?.deviceType
    }

}