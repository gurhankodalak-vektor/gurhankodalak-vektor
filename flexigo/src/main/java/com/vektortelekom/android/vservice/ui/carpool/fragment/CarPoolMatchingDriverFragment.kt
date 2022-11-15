package com.vektortelekom.android.vservice.ui.carpool.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.CarPoolListModel
import com.vektortelekom.android.vservice.data.model.ChooseRiderRequest
import com.vektortelekom.android.vservice.databinding.CarpoolMatchingFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import com.vektortelekom.android.vservice.ui.carpool.adapter.CarPoolMatchedAdapter
import javax.inject.Inject

class CarPoolMatchingDriverFragment : BaseFragment<CarPoolViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolMatchingFragmentBinding

    private var matchedAdapter: CarPoolMatchedAdapter? = null

    @Volatile
    private var myLocation: Location? = null

    private lateinit var locationClient: FusedLocationClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CarpoolMatchingFragmentBinding>(inflater, R.layout.carpool_matching_fragment, container, false).apply {
            lifecycleOwner = this@CarPoolMatchingDriverFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        }
        else {
            onLocationPermissionFailed()
        }

        binding.textviewMatchedTitle.text = resources.getText(R.string.riders_picking_up)

        matchedAdapter = CarPoolMatchedAdapter("drivers_match", object : CarPoolMatchedAdapter.CarPoolItemClickListener{
            override fun onCancelClicked(item: CarPoolListModel) {
                if (viewModel.isDriver.value == true){
                    showEndPoolingConfirmation(item.id, item.name)
                }
            }

            override fun onApproveClicked(item: CarPoolListModel) {}

            override fun onNavigateClicked(item: CarPoolListModel) {
                navigateToMap(myLocation?.latitude?:0.0, myLocation?.longitude?:0.0, item.homeLocation!!.latitude, item.homeLocation.longitude)
            }

            override fun onCallClicked(item: CarPoolListModel) {
                val phoneNumber = item.phoneNumber

                if (phoneNumber == null)
                    viewModel.navigator?.handleError(Exception(getString(R.string.error_empty_phone_number)))
                else{
                    AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
                        .setTitle(getString(R.string.call_2))
                        .setMessage(getString(R.string.will_call, phoneNumber))
                        .setPositiveButton(getString(R.string.Generic_Ok)) { d, _ ->
                            d.dismiss()
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(phoneNumber)))
                            startActivity(intent)
                        }
                        .setNegativeButton(getString(R.string.cancel)) { d, _ ->
                            d.dismiss()
                        }
                        .create().show()
                }


            }

        })

        viewModel.approvedRiders.observe(viewLifecycleOwner){
            if (it != null && it.isNotEmpty()) {
                matchedAdapter?.setList(it)
                binding.recyclerviewMatchedRiders.adapter = matchedAdapter
            } else
                activity?.finish()
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            }
            )
    }


    private fun navigateToMap(userLocationLatitude: Double, userLocationLongitude: Double, targetLocationLatitude: Double, targetLocationLongitude: Double) {

        val baseUri = "geo:%s,%s?q=%s,%s"
        val uri = String.format(baseUri, userLocationLatitude, userLocationLongitude, targetLocationLatitude, targetLocationLongitude)
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e1: Exception) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(java.lang.String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", userLocationLatitude, userLocationLongitude, targetLocationLatitude, targetLocationLongitude)))
                    startActivity(browserIntent)
                } catch (e2: java.lang.Exception) {
                    AppLogger.e(e, "NavigationAppNotFound")
                    Toast.makeText(requireContext(), R.string.Maps_No_Exist, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEndPoolingConfirmation(driverPersonnelId: Long, driverName: String) {

        val dialog = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
        dialog.setCancelable(false)
        dialog.setMessage(resources.getString(R.string.endpool_carpooling, driverName))
        dialog.setPositiveButton(resources.getString(R.string.confirm)) { d, _ ->

            (requireActivity() as BaseActivity<*>).showPd()
            val request = ChooseRiderRequest(driverPersonnelId, false, null)
            viewModel.setChooseRider(request, true)

            d.dismiss()
        }
        dialog.setNegativeButton(resources.getString(R.string.cancel)) { d, _ ->
            d.dismiss()
        }

        dialog.show()

    }

    override fun onLocationPermissionFailed() {}

    override fun onLocationPermissionOk() {

        locationClient = FusedLocationClient(requireContext())

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            @SuppressLint("MissingPermission")
            override fun onLocationUpdated(location: Location) {

                myLocation = location
                AppDataManager.instance.currentLocation = location
            }

            override fun onLocationFailed(message: String) {
                if(activity?.isFinishing != false || activity?.isDestroyed != false) {
                    return
                }

                when (message) {
                    FusedLocationClient.ERROR_LOCATION_DISABLED -> locationClient.showLocationSettingsDialog()
                    FusedLocationClient.ERROR_LOCATION_MODE -> {
                        locationClient.showLocationSettingsDialog()
                    }
                    FusedLocationClient.ERROR_TIMEOUT_OCCURRED -> {
                        (activity as BaseActivity<*>).handleError(RuntimeException(getString(R.string.location_timeout)))
                    }
                }
            }

        })
    }

    override fun getViewModel(): CarPoolViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CarPoolViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CarPoolMatchingDriverFragment"
        fun newInstance() = CarPoolMatchingDriverFragment()
    }


}