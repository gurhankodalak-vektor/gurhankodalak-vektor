package com.vektortelekom.android.vservice.ui.carpool

import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.VanpoolApprovalType
import com.vektortelekom.android.vservice.databinding.VanpoolDriverActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.home.HomeViewModel
import com.vektortelekom.android.vservice.ui.menu.MenuActivity
import com.vektortelekom.android.vservice.ui.vanpool.fragment.VanpoolDriverApprovalFragment
import javax.inject.Inject

class CarPoolActivity : BaseActivity<HomeViewModel>(), PermissionsUtils.LocationStateListener {
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: HomeViewModel

    lateinit var binding: VanpoolDriverActivityBinding

    private lateinit var locationClient: FusedLocationClient

    private var versionedRouteId: Long? = null
    private var approvalItemId: Long? = null
    private var workgroupInstanceId: Long? = null
    private var approvalType: VanpoolApprovalType? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<VanpoolDriverActivityBinding>(this, R.layout.vanpool_driver_activity).apply {
            lifecycleOwner = this@CarPoolActivity
        }

        if ((this@CarPoolActivity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        }
        else {
            onLocationPermissionFailed()
        }

        workgroupInstanceId = intent.getLongExtra("workgroupInstanceId", 0L)
        versionedRouteId = intent.getLongExtra("versionedRouteId", 0L)
        approvalItemId = intent.getLongExtra("approvalItemId", 0L)
        approvalType = intent.getSerializableExtra("approvalType") as? VanpoolApprovalType

        viewModel.instanceId.value = workgroupInstanceId
        viewModel.versionedRouteId.value = versionedRouteId
        viewModel.approvalType.value = approvalType
        viewModel.approvalItemId.value = approvalItemId

        showVanpoolFragment()


        viewModel.isForDrivingLicence.observe(this) {
            if (it != null && it) {
                showMenuDrivingLicenceFragment()
            }
        }

    }
    private fun showMenuDrivingLicenceFragment() {

        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("is_for_driving_license", true)
        startActivity(intent)

        finish()
    }

    private fun showVanpoolFragment() {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, VanpoolDriverApprovalFragment.newInstance(), VanpoolDriverApprovalFragment.TAG)
                .commit()

    }


    override fun getViewModel(): HomeViewModel {
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        return viewModel
    }

    companion object {
        const val TAG: String = "VanPoolDriverActivity"

        fun newInstance() = CarPoolActivity()

    }

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

                if(this@CarPoolActivity.isFinishing || this@CarPoolActivity.isDestroyed) {
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
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

}