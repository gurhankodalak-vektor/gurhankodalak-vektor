package com.vektortelekom.android.vservice.ui.flexiride.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.PoolcarAndFlexirideModel
import com.vektortelekom.android.vservice.databinding.FlexiridePlannedFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.flexiride.FlexirideViewModel
import com.vektortelekom.android.vservice.utils.convertBackendDateToHourMin
import javax.inject.Inject

class FlexiridePlannedFragment: BaseFragment<FlexirideViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: FlexirideViewModel

    lateinit var binding: FlexiridePlannedFragmentBinding

     var googleMap: GoogleMap? = null

    private var pickUpPinIcon: BitmapDescriptor? = null
    private var dropOffPinIcon: BitmapDescriptor? = null

    private var offerFromMarker: Marker? = null
    private var offerToMarker: Marker? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var vehicleIcon: BitmapDescriptor? = null

    private var vehicleMarker: Marker? = null

    private var vehicleRefreshHandler: Handler? = null

    private var lastVehicleUpdateTime = 0L
    private val timeIntervalToUpdateVehicle = 10L * 1000L

    @Volatile
    private var myLocation: Location? = null
    private lateinit var locationClient: FusedLocationClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<FlexiridePlannedFragmentBinding>(inflater, R.layout.flexiride_planned_fragment, container, false).apply {
            lifecycleOwner = this@FlexiridePlannedFragment
            viewModel = this@FlexiridePlannedFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync { googleMap ->

            this.googleMap = googleMap

            vehicleRefreshHandler = Handler(requireActivity().mainLooper)

            pickUpPinIcon = BitmapDescriptorFactory.fromBitmap(createStoreMarker(R.layout.flexiride_map_marker_pick_up))
            dropOffPinIcon = BitmapDescriptorFactory.fromBitmap(createStoreMarker(R.layout.flexiride_map_marker_drop_off))
            vehicleIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_vehicle)

            continueAfterMapInitialized()

            viewModel.startedFlexiride.observe(viewLifecycleOwner) {

                binding.layoutFlexirideRoute.visibility = View.VISIBLE
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                it.fromLocation?.let { fromLocation ->
                    binding.textViewTaxiMin.text = it.distanceInMeter.toString().plus(" ").plus(getString(R.string.hint_km).lowercase())
                    binding.textViewTaxiFromAddress.text = getString(R.string.pick_up_address, fromLocation.address)
                    binding.textViewTaxiToAddress.text = getString(R.string.arrival_address, it.toLocation?.address?:"")
                    binding.textViewTaxiFromTime.text = it.flexirideRequest?.requestedPickupTime.convertBackendDateToHourMin()
                    binding.textViewTaxiToTime.text = it.flexirideRequest?.requestedDeliveryTime.convertBackendDateToHourMin()

                    offerFromMarker = googleMap.addMarker(MarkerOptions().position(LatLng(fromLocation.latitude, fromLocation.longitude)).icon(pickUpPinIcon))
                    offerToMarker = googleMap.addMarker(MarkerOptions().position(LatLng(it.toLocation?.latitude?:0.0, it.toLocation?.longitude?:0.0)).icon(dropOffPinIcon))

                    val minLat = if(fromLocation.latitude < (it.toLocation?.latitude ?: 0.0)) fromLocation.latitude else it.toLocation?.latitude
                    val maxLat = if(fromLocation.latitude > (it.toLocation?.latitude ?: 0.0)) fromLocation.latitude else it.toLocation?.latitude
                    val minLng = if(fromLocation.longitude < (it.toLocation?.longitude ?: 0.0)) fromLocation.longitude else it.toLocation?.longitude
                    val maxLng = if(fromLocation.longitude > (it.toLocation?.longitude ?: 0.0)) fromLocation.longitude else it.toLocation?.longitude

                    val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat?:0.0, minLng?:0.0), LatLng(maxLat?:0.0, maxLng?:0.0)), 350)
                    googleMap.moveCamera(cu)
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

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.buttonMyLocation.setOnClickListener {
            myLocation?.let {
                val cu = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                googleMap?.animateCamera(cu)
            }

        }

    }

    private fun continueAfterMapInitialized() {
        if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
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

    override fun onLocationPermissionFailed() {
    }

    private fun createStoreMarker(@LayoutRes layoutId: Int): Bitmap {
        val markerView = layoutInflater.inflate(layoutId, null)

        markerView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

        val bitmap = Bitmap.createBitmap(markerView.measuredWidth, markerView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)
        return bitmap
    }

    private val vehicleRefreshRunnable = Runnable {
        viewModel.updateFlexiride()
    }

    private fun fillVehicleLocation(response: PoolcarAndFlexirideModel) {

        val latitude: Double
        val longitude: Double
        val direction: Float
        try {

            latitude = response.driver?.location?.latitude ?: 0.0
            longitude = response.driver?.location?.longitude ?: 0.0
            direction = /*response.DIRECTION?.toDouble()?.toFloat() ?:*/ 0f
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
        }
    }

    override fun getViewModel(): FlexirideViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[FlexirideViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "FlexiridePlannedFragment"

        fun newInstance() = FlexiridePlannedFragment()

    }

}