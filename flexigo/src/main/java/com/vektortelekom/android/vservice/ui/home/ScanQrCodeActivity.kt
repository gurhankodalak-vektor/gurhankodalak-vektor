package com.vektortelekom.android.vservice.ui.home

import android.content.ClipData
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.QrCodeActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.home.fragment.ScanQrCodeFragment
import com.vektortelekom.android.vservice.ui.home.fragment.ScanQrReaderFragment
import javax.inject.Inject

class ScanQrCodeActivity : BaseActivity<HomeViewModel>(), HomeNavigator, PermissionsUtils.LocationStateListener, PermissionsUtils.CameraStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: HomeViewModel

    lateinit var binding: QrCodeActivityBinding

    private lateinit var locationClient: FusedLocationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<QrCodeActivityBinding>(this, R.layout.qr_code_activity).apply {
                lifecycleOwner = this@ScanQrCodeActivity
            }
        viewModel.navigator = this


        if ((this@ScanQrCodeActivity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        }
        else {
            onLocationPermissionFailed()
        }

        viewModel.isCameNotification = intent.getBooleanExtra("isCameNotification", false)

        if (AppDataManager.instance.isQrAutoOpen && checkAndRequestCameraPermission(this)) {
            onCameraPermissionOk()
        } else {
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.qr_code_fragment,
                    ScanQrCodeFragment.newInstance(),
                    ScanQrCodeFragment.TAG
                )
                .commit()
        }

    }

    override fun onCameraPermissionOk() {
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.qr_code_fragment,
                ScanQrReaderFragment.newInstance(),
                ScanQrReaderFragment.TAG
            )
            .commit()
    }

    override fun onCameraPermissionFailed() {
        AppDialog.Builder(this)
            .setIconVisibility(true)
            .setTitle(R.string.permission_camera_title)
            .setSubtitle(R.string.permission_camera_subtitle)
            .setOkButton(R.string.settings) { d ->
                d.dismiss()
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (t: Throwable) {
                    AppLogger.e("Application permission navigation failed.", t)
                }
            }
            .create().show()
    }

    override fun onLocationPermissionFailed() {}


    override fun onLocationPermissionOk() {
        locationClient = FusedLocationClient(this)

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            override fun onLocationUpdated(location: Location) {

                viewModel.myLocation = location
                locationClient.stop()
                AppDataManager.instance.currentLocation = location

            }

            override fun onLocationFailed(message: String) {

                if(this@ScanQrCodeActivity.isFinishing || this@ScanQrCodeActivity.isDestroyed) {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

    override fun getViewModel(): HomeViewModel {
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        return viewModel
    }

    companion object {
        const val TAG: String = "ScanQrCodeActivity"
        fun newInstance() = ScanQrCodeActivity()
    }

    override fun showMenuActivity(view: View?)  = Unit
    override fun showPoolCarActivity(view: View?)  = Unit
    override fun showPoolCarReservationsActivity(view: View?)  = Unit
    override fun showPoolCarAddReservationActivity(isIntercity: Boolean)  = Unit
    override fun showStartTaxiActivity(view: View?)  = Unit
    override fun showReportTaxiActivity(view: View?) = Unit
    override fun showTaxiListFragment(view: View?)  = Unit
    override fun showFlexiRideActivity(type: Int) = Unit
    override fun showFlexiRideListActivity(view: View?) = Unit
    override fun showCalendarActivity(view: View?) = Unit
    override fun showPastUsesActivity(view: View?) = Unit
}