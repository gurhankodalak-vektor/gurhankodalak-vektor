package com.vektortelekom.android.vservice.ui.flexiride.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.FlexirideRequestDetailBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.flexiride.FlexirideViewModel
import com.vektortelekom.android.vservice.ui.shuttle.map.ShuttleInfoWindowAdapter
import com.vektortelekom.android.vservice.utils.*
import org.joda.time.DateTime
import javax.inject.Inject

class FlexiRideRequestDetailFragment : BaseFragment<FlexirideViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: FlexirideViewModel

    lateinit var binding: FlexirideRequestDetailBinding

    private var googleMap: GoogleMap? = null

    private var workplaceIcon: BitmapDescriptor? = null
    private var toLocationIcon: BitmapDescriptor? = null
    private var homeIcon: BitmapDescriptor? = null

    private lateinit var locationClient: FusedLocationClient

    var destination : DestinationModel? = null

    var phoneNumber : String? = null


    private var vehicleIcon: BitmapDescriptor? = null

    private var vehicleMarker: Marker? = null

    private var vehicleRefreshHandler: Handler? = null

    private var lastVehicleUpdateTime = 0L
    private val timeIntervalToUpdateVehicle = 10L * 1000L

    @Volatile
    private var myLocation: Location? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<FlexirideRequestDetailBinding>(inflater, R.layout.flexiride_request_detail, container, false).apply {
            lifecycleOwner = this@FlexiRideRequestDetailFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)


        binding.mapView.getMapAsync { map ->
            googleMap = map
            googleMap!!.uiSettings.isZoomControlsEnabled = true

            vehicleRefreshHandler = Handler(requireActivity().mainLooper)

            if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
                onLocationPermissionOk()
            }
            else {
                onLocationPermissionFailed()
            }

            googleMap?.setInfoWindowAdapter(ShuttleInfoWindowAdapter(requireActivity()))

            workplaceIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_workplace)
            toLocationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_route_to_yellow)
            homeIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_home)
            vehicleIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_flexiride)

            viewModel.startedFlexiride.observe(viewLifecycleOwner) {

                if(it.vehicle?.plate == "" || it.vehicle?.plate == null) {
                    binding.textviewPlateValue.text = getString(R.string.to_be_assigned)
                    binding.textviewPlateValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
                }
                else{
                    binding.textviewPlateValue.text = it.vehicle?.plate
                    binding.textviewPlateValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkNavyBlue))
                }

                if (it.driver?.fullName != null){
                    binding.textviewDriverValue.text = it.driver?.fullName
                    binding.textviewPlateValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkNavyBlue))
                    phoneNumber = it.driver?.mobile

                } else{
                    binding.textviewDriverValue.text = getString(R.string.to_be_assigned)
                    binding.textviewDriverValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
                    binding.imageviewCall.visibility = View.GONE
                }


                if ((it.driver != null && it.driver?.id != null) && (it.vehicle != null && it.vehicle?.id != null)){

                    binding.buttonCancelRequest.visibility = View.GONE
                    binding.textviewTotal.text = getString(R.string.remaining)
                } else{

                    binding.buttonCancelRequest.visibility = View.VISIBLE
                    binding.textviewTotal.text = getString(R.string.total)
                }

                
                it.fromLocation?.let { fromLocation ->


                    binding.textviewTotalValue.text = it.flexirideRequest?.travelTimeInMinute.toString().plus(" ").plus(getString(R.string.short_minute)).plus(", ")
                        .plus(String.format("%.2f", it.distanceInMeter?.convertMetersToMile()).plus(" ").plus(getString(R.string.mile_short)))
                    binding.textviewPickUpValue.text = it.flexirideRequest?.requestedPickupTime.convertBackendDateToLong().convertToShuttleDateTime()

                    binding.textviewEstimatedArrivalValue.text = DateTime(it.flexirideRequest?.requestedPickupTime.convertBackendDateToLong())
                        .plusMinutes(it.flexirideRequest?.travelTimeInMinute?.toInt()?:0).toDate().convertForBackend2().convertBackendDateToLong().convertToShuttleDateTime()


                    googleMap!!.addMarker(MarkerOptions().position(LatLng(fromLocation.latitude, fromLocation.longitude)).icon(workplaceIcon))?.tag = it.fromLocation!!.address
                    googleMap!!.addMarker(MarkerOptions().position(LatLng(it.toLocation?.latitude?:0.0, it.toLocation?.longitude?:0.0)).icon(toLocationIcon))?.tag = it.toLocation?.address

                    val minLat = if(fromLocation.latitude < (it.toLocation?.latitude ?: 0.0)) fromLocation.latitude else it.toLocation?.latitude
                    val maxLat = if(fromLocation.latitude > (it.toLocation?.latitude ?: 0.0)) fromLocation.latitude else it.toLocation?.latitude
                    val minLng = if(fromLocation.longitude < (it.toLocation?.longitude ?: 0.0)) fromLocation.longitude else it.toLocation?.longitude
                    val maxLng = if(fromLocation.longitude > (it.toLocation?.longitude ?: 0.0)) fromLocation.longitude else it.toLocation?.longitude

//                    val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat?:0.0, minLng?:0.0), LatLng(maxLat?:0.0, maxLng?:0.0)), 350)
//                    googleMap?.moveCamera(cu)

                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(fromLocation.latitude, fromLocation.longitude), 12f))
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng( it.toLocation?.latitude?:0.0,  it.toLocation?.longitude?:0.0), 12f))

                }

                viewModel.updatedFlexiride.observe(viewLifecycleOwner) { poolcarAndFlexirideModel ->
                    if (poolcarAndFlexirideModel != null) {
                        if (googleMap != null) {
                            fillVehicleLocation(poolcarAndFlexirideModel)
                            lastVehicleUpdateTime = System.currentTimeMillis()
                            vehicleRefreshHandler?.removeCallbacksAndMessages(null)
                            vehicleRefreshHandler?.postDelayed(vehicleRefreshRunnable, timeIntervalToUpdateVehicle)
                        }

                    }
                }

            }

        }

        binding.buttonMyLocation.setOnClickListener {
            myLocation?.let {
                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 14f)
                googleMap?.moveCamera(cu)

            }

        }


        binding.imageviewCall.setOnClickListener {

            if (phoneNumber == null)
                viewModel.navigator?.handleError(Exception(getString(R.string.error_empty_phone_number)))
            else {
                AppDialog.Builder(requireContext())
                    .setCloseButtonVisibility(false)
                    .setIconVisibility(false)
                    .setTitle(getString(R.string.call_2))
                    .setSubtitle(getString(R.string.will_call, phoneNumber))
                    .setOkButton(getString(R.string.Generic_Ok)) { d ->
                        d.dismiss()

                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(phoneNumber)))
                        startActivity(intent)

                    }
                    .setCancelButton(getString(R.string.cancel)) { d ->
                        d.dismiss()
                    }
                    .create().show()
            }
        }

        binding.imageviewBack.setOnClickListener {
            activity?.finish()
        }

        binding.buttonCancelRequest.setOnClickListener {
            viewModel.deleteFlexiride(viewModel.startedFlexiride.value?.id ?: 0)
        }


    }
    private fun fillVehicleLocation(response: PoolcarAndFlexirideModel) {

        val latitude: Double
        val longitude: Double
        val direction: Float
        try {

            latitude = response.driver?.location?.latitude ?: 0.0
            longitude = response.driver?.location?.longitude ?: 0.0
            direction = 0f
        } catch (e: java.lang.Exception) {
            vehicleMarker?.remove()
            vehicleMarker = null
            return
        }


        if (vehicleMarker == null) {
            vehicleMarker = googleMap?.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).icon(vehicleIcon).rotation(direction))
        } else {
            vehicleMarker?.position = LatLng(latitude, longitude)
            vehicleMarker?.rotation = direction

            val cu = CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 14f)
            googleMap?.moveCamera(cu)
        }
    }

    private val vehicleRefreshRunnable = Runnable {
        viewModel.updateFlexiride()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()

        val currentTime = System.currentTimeMillis()

        val timeDiff = currentTime - lastVehicleUpdateTime

        if (timeDiff > timeIntervalToUpdateVehicle) {
            viewModel.updateFlexiride()
        } else {
            vehicleRefreshHandler?.removeCallbacksAndMessages(null)
            vehicleRefreshHandler?.postDelayed(vehicleRefreshRunnable, timeIntervalToUpdateVehicle - timeDiff)
        }

    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()

        vehicleRefreshHandler?.removeCallbacksAndMessages(null)
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun getViewModel(): FlexirideViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[FlexirideViewModel::class.java] }
                ?: throw Exception("Invalid Activity")

        return viewModel

    }

    companion object {
        const val TAG: String = "FlexirideRequestDetailFragment"
        fun newInstance() = FlexiRideRequestDetailFragment()

    }

    override fun onLocationPermissionFailed() {
    }
    override fun onLocationPermissionOk() {

        locationClient = FusedLocationClient(requireContext())

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            @SuppressLint("MissingPermission")
            override fun onLocationUpdated(location: Location) {

                googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                googleMap?.isMyLocationEnabled = true


                binding.buttonMyLocation.visibility = View.VISIBLE

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
}