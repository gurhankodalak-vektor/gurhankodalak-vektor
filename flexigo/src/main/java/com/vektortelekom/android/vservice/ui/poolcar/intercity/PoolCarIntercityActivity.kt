package com.vektortelekom.android.vservice.ui.poolcar.intercity

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.PoolcarAndFlexirideModel
import com.vektortelekom.android.vservice.databinding.PoolCarIntercityActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.menu.MenuActivity
import com.vektortelekom.android.vservice.ui.menu.fragment.MenuPdfViewerFragment
import com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment.PoolCarIntercityBeforeStartFragment
import com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment.PoolCarIntercityFinishFragment
import com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment.PoolCarIntercityRentalFragment
import com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment.PoolCarIntercityStartFragment
import com.vektortelekom.android.vservice.utils.convertBackendDateToIntercityString
import javax.inject.Inject

class PoolCarIntercityActivity : BaseActivity<PoolCarIntercityViewModel>(),
        PoolCarIntercityNavigator, PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarIntercityViewModel

    private lateinit var binding: PoolCarIntercityActivityBinding

    private lateinit var locationClient: FusedLocationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<PoolCarIntercityActivityBinding>(this, R.layout.pool_car_intercity_activity).apply {
            lifecycleOwner = this@PoolCarIntercityActivity
            viewModel = this@PoolCarIntercityActivity.viewModel
        }
        viewModel.navigator = this

        intent?.let {
            if(intent.hasExtra("startReservation")) {
                val startReservation = intent.getParcelableExtra<PoolcarAndFlexirideModel>("startReservation")
                startReservation?.let {
                    viewModel.id = it.id
                    viewModel.fromLocation.value = it.fromLocation?.name
                    viewModel.finishDate.value = it.flexirideRequest?.requestedDeliveryTime?.convertBackendDateToIntercityString()

                    var vehicleText = it.vehicle?.plate

                    it.vehicle?.make?.let {  make ->
                        vehicleText = vehicleText.plus(" - ")
                                .plus(make)
                        it.vehicle?.model?.let { model ->
                            vehicleText = vehicleText.plus(" ")
                                    .plus(model)
                        }
                    }

                    if(vehicleText.isNullOrBlank()) {
                        vehicleText = getString(R.string.vehicle_not_defined_yet)
                    }

                    viewModel.vehicleInfo.value = vehicleText
                }
                showBeforeStartFragment()
            }
            else if(intent.hasExtra("rental")){
                val rental = intent.getParcelableExtra<PoolcarAndFlexirideModel>("rental")
                rental?.let {
                    viewModel.id = rental.id
                    viewModel.licensePlate.value = rental.flexirideRequest?.vehiclePlate
                    viewModel.startKm.value = rental.flexirideRequest?.startOdometer.toString()
                    viewModel.vehicleMake.value = rental.flexirideRequest?.vehicleMake?.plus(" ").plus(rental.flexirideRequest?.vehicleModel)
                    showRentalFragment()
                }

            }
        }


        if (checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        }
        else {
            onLocationPermissionFailed()
        }
    }

    private fun showBeforeStartFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.root_fragment, PoolCarIntercityBeforeStartFragment.newInstance(), PoolCarIntercityBeforeStartFragment.TAG)
                .commit()
    }

    override fun showStartFragment(view: View?) {
        if(viewModel.isStartAgreementApproved.value == true) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.root_fragment, PoolCarIntercityStartFragment.newInstance(), PoolCarIntercityStartFragment.TAG)
                    .addToBackStack(null)
                    .commit()
        }
        else {
            FlexigoInfoDialog.Builder(this)
                    .setIconVisibility(false)
                    .setCancelable(false)
                    .setText1(getString(R.string.rental_start_checkbox_unchecked_error_text))
                    .setOkButton(getString(R.string.Generic_Ok)) {
                        it.dismiss()
                    }
                    .create()
                    .show()
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

    override fun showRentalFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.root_fragment, PoolCarIntercityRentalFragment.newInstance(), PoolCarIntercityRentalFragment.TAG)
                .commit()
    }

    override fun getViewModel(): PoolCarIntercityViewModel {
        viewModel = ViewModelProvider(this, factory)[PoolCarIntercityViewModel::class.java]
        return viewModel
    }

    override fun backPressed(view: View?) {
        onBackPressed()
    }

    override fun onBackPressed() {
        if(supportFragmentManager.findFragmentByTag(PoolCarIntercityRentalFragment.TAG) != null && supportFragmentManager.findFragmentByTag(PoolCarIntercityFinishFragment.TAG) == null) {
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

    override fun showMenuActivity(view: View?) {
        startActivity(Intent(this, MenuActivity::class.java))
    }

    override fun showFinishFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarIntercityFinishFragment.newInstance(), PoolCarIntercityFinishFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun onLocationPermissionOk() {

        locationClient = FusedLocationClient(this)

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            @SuppressLint("MissingPermission")
            override fun onLocationUpdated(location: Location) {


                viewModel.location = location

                locationClient.stop()
                AppDataManager.instance.currentLocation = location
            }

            override fun onLocationFailed(message: String) {

                if(isFinishing || isDestroyed) {
                    return
                }

                when (message) {
                    FusedLocationClient.ERROR_LOCATION_DISABLED -> locationClient.showLocationSettingsDialog()
                    FusedLocationClient.ERROR_LOCATION_MODE -> {
                        locationClient.showLocationSettingsDialog()
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

}