package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.ParkModel
import com.vektortelekom.android.vservice.databinding.PoolCarMainFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.ui.poolcar.adapter.ParkingLotsAdapter
import com.vektortelekom.android.vservice.utils.bitmapDescriptorFromVector
import javax.inject.Inject

class PoolCarMainFragment: BaseFragment<PoolCarViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    lateinit var binding: PoolCarMainFragmentBinding

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var googleMap: GoogleMap? = null

    private var parkLotsMarkers: MutableList<Marker> = mutableListOf()

    private var selectedMarker: Marker? = null

    private var parkIcon: BitmapDescriptor? = null
    private var parkIconSelected: BitmapDescriptor? = null

    private var isMarkerChangeFromCode = false

    @Volatile
    private var myLocation: Location? = null
    private lateinit var locationClient: FusedLocationClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarMainFragmentBinding>(inflater, R.layout.pool_car_main_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarMainFragment
            viewModel = this@PoolCarMainFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getStations()

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        binding.mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync {

            googleMap = it

            parkIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_station_marker)
            parkIconSelected = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_station_marker_selected)

            getStations()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            googleMap?.setOnMarkerClickListener { selectedMarker ->

                for(index in parkLotsMarkers.indices) {
                    val marker = parkLotsMarkers[index]
                    if(marker == selectedMarker) {
                        isMarkerChangeFromCode = true
                        binding.recyclerViewParkingLots.smoothScrollToPosition(index)
                        changeSelectedMarker(marker)
                        break
                    }
                }

                true

            }

        }

        binding.buttonMyLocation.setOnClickListener {
            myLocation?.let {
                val cu = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                googleMap?.animateCamera(cu)
            }
        }

        if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        }
        else {
            onLocationPermissionFailed()
        }

    }

    private fun getStations() {
        viewModel.stations.observe(viewLifecycleOwner) { parks ->

            binding.recyclerViewParkingLots.adapter = ParkingLotsAdapter(parks, object : ParkingLotsAdapter.ParkListener {
                override fun parkSelected(parkModel: ParkModel) {
                    viewModel.selectedStation.value = parkModel
                    viewModel.navigator?.showPoolCarParkFragment(null)
                }

            })

            binding.recyclerViewParkingLots.addOnItemChangedListener { _, i ->

                if (isMarkerChangeFromCode) {
                    isMarkerChangeFromCode = false
                    return@addOnItemChangedListener
                }

                if (parkLotsMarkers.size > i) {
                    changeSelectedMarker(parkLotsMarkers[i])
                }

            }

            parkLotsMarkers = mutableListOf()

            googleMap?.let {
                for (index in parks.indices) {

                    val marker = it.addMarker(MarkerOptions().position(LatLng(parks[index].latitude
                            ?: 0.0, parks[index].longitude ?: 0.0)))
                    marker?.tag = parks[index]
                    parkLotsMarkers.add(marker!!)
                    if (index == 0) {
                        marker.setIcon(parkIconSelected)
                        selectedMarker = marker
                    } else {
                        marker.setIcon(parkIcon)
                    }

                }
            }

            if (parks.isNotEmpty()) {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(parks[0].latitude
                        ?: 0.0, parks[0].longitude ?: 0.0), 11f))
            }

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        }

    }

    private fun changeSelectedMarker(marker: Marker) {
        selectedMarker?.setIcon(parkIcon)
        marker.setIcon(parkIconSelected)
        selectedMarker = marker
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 11f))
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
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

    override fun getViewModel(): PoolCarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarMainFragment"

        fun newInstance() = PoolCarMainFragment()

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

                locationClient.stop()
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

}