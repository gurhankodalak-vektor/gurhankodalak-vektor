package com.vektortelekom.android.vservice.ui.taxi.fragment

import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.TaxiFindLocationFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.menu.adapters.SearchPlaceResultsAdapter
import com.vektortelekom.android.vservice.ui.taxi.TaxiViewModel
import com.vektortelekom.android.vservice.utils.AutoCompleteManager
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class TaxiFindLocationFragment: BaseFragment<TaxiViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: TaxiViewModel

    lateinit var binding: TaxiFindLocationFragmentBinding

    private var googleMap: GoogleMap? = null

    @Volatile
    private var myLocation: Location? = null
    private lateinit var locationClient: FusedLocationClient

    private lateinit var placesClient: PlacesClient

    private lateinit var searchPlacesResultsAdapter: SearchPlaceResultsAdapter

    var isStart = true
    var isReport = 1

    private var isLocationFailedBefore = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<TaxiFindLocationFragmentBinding>(inflater, R.layout.taxi_find_location_fragment, container, false).apply {
            lifecycleOwner = this@TaxiFindLocationFragment
            viewModel = this@TaxiFindLocationFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            isStart = it.getInt("is_start", 1) == 1
            isReport = it.getInt("is_report", 1)
        }

        binding.mapView.onCreate(savedInstanceState)

        placesClient = Places.createClient(requireContext())

        searchPlacesResultsAdapter = SearchPlaceResultsAdapter(ArrayList(), object : SearchPlaceResultsAdapter.SearchItemClickListener {

            override fun onItemClicked(autocompletePrediction: AutocompletePrediction) {
                binding.cardViewSearchResults.visibility = View.GONE
                (requireActivity() as BaseActivity<*>).showPd()
                placesClient.fetchPlace(FetchPlaceRequest.builder(autocompletePrediction.placeId, listOf(Place.Field.LAT_LNG))
                        .setSessionToken(AutocompleteSessionToken.newInstance())
                        .build())
                        .addOnSuccessListener { response ->
                            (requireActivity() as BaseActivity<*>).dismissPd()
                            val cu = response.place.latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 19f) }
                            if (cu != null) {
                                googleMap?.moveCamera(cu)
                            }

                            when(isReport) {
                                0 -> {
                                    if(isStart) {
                                        viewModel.startLocationStart.value = response.place.latLng
                                        viewModel.startLocationTextStart.value = response.place.address
                                    }
                                }
                                1 -> {
                                    if(isStart) {
                                        viewModel.startLocationReport.value = response.place.latLng
                                        viewModel.startLocationTextReport.value = response.place.address
                                    }
                                    else {
                                        viewModel.endLocationReport.value = response.place.latLng
                                        viewModel.endLocationTextReport.value = response.place.address
                                    }
                                }
                                2 -> {
                                    if(!isStart.not()) {
                                        viewModel.endLocationFinish.value = response.place.latLng
                                        viewModel.endLocationTextFinish.value = response.place.address
                                    }
                                }
                            }
                            binding.editTextResult.setText(response.place.address)
                        }
                        .addOnFailureListener {
                            (requireActivity() as BaseActivity<*>).dismissPd()
                            viewModel.navigator?.handleError(it)
                        }

            }


        })

        binding.recyclerViewSearchResults.adapter = searchPlacesResultsAdapter

        binding.editTextSearch.setOnEditorActionListener { _, actionId, _ ->

            (requireActivity() as BaseActivity<*>).showPd()

            if (actionId == EditorInfo.IME_ACTION_NEXT) {

                val searchKey = binding.editTextSearch.text.toString()
                val request = AutoCompleteManager.instance.getAutoCompleteRequest(searchKey, AppDataManager.instance.currentLocation)

                placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->

                    (requireActivity() as BaseActivity<*>).dismissPd()

                    if (response != null) {
                        binding.cardViewSearchResults.visibility = View.VISIBLE
                        searchPlacesResultsAdapter.setSearchList(response.autocompletePredictions)
                    }

                }
                        .addOnFailureListener {
                            (requireActivity() as BaseActivity<*>).dismissPd()
                        }

            }
            return@setOnEditorActionListener true
        }

        binding.mapView.getMapAsync { googleMap ->

            this.googleMap = googleMap

            continueAfterMapInitialized()

            googleMap.setOnCameraIdleListener {

                when(isReport) {
                    0 -> {
                        if(isStart) {
                            viewModel.startLocationStart.value = googleMap.cameraPosition.target
                            viewModel.isStartLocationChangedManuelly = true
                        }
                    }
                    1 -> {
                        if(isStart) {
                            viewModel.startLocationReport.value = googleMap.cameraPosition.target
                        }
                        else {
                            viewModel.endLocationReport.value = googleMap.cameraPosition.target
                        }
                    }
                    2 -> {
                        if(isStart.not()) {
                            viewModel.isEndLocationChangedManuelly = true
                            viewModel.endLocationFinish.value = googleMap.cameraPosition.target
                        }
                    }
                }


                val geoCoder = Geocoder(requireContext(), Locale("tr-TR"))

                try {
                    val addresses = geoCoder.getFromLocation(googleMap.cameraPosition.target.latitude, googleMap.cameraPosition.target.longitude, 1)

                    if(addresses.size > 0) {
                        val address = addresses[0]
                        binding.editTextResult.setText(address.getAddressLine(0))

                        when(isReport) {
                            0 -> {
                                if(isStart) {
                                    viewModel.startLocationTextStart.value = address.getAddressLine(0)
                                }
                            }
                            1 -> {
                                if(isStart) {
                                    viewModel.startLocationTextReport.value = address.getAddressLine(0)
                                }
                                else {
                                    viewModel.endLocationTextReport.value = address.getAddressLine(0)
                                }
                            }
                            2 -> {
                                if(isStart.not()) {
                                    viewModel.endLocationTextFinish.value = address.getAddressLine(0)
                                }
                            }
                        }

                    }
                }
                catch (e: Exception) {
                    when(isReport) {
                        0 -> {
                            if(isStart) {
                                viewModel.startLocationTextStart.value = ""
                            }
                        }
                        1 -> {
                            if(isStart) {
                                viewModel.startLocationTextReport.value = ""
                            }
                            else {
                                viewModel.endLocationTextReport.value = ""
                            }
                        }
                        2 -> {
                            if(isStart.not()) {
                                viewModel.endLocationTextFinish.value = ""
                            }
                        }
                    }
                }


            }

        }

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
        else {
            onLocationPermissionFailed()
        }
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

    override fun getViewModel(): TaxiViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[TaxiViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "TaxiFindLocationFragment"
        fun newInstance() = TaxiFindLocationFragment()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

    override fun onLocationPermissionOk() {

        when(isReport) {
            0 -> {
                if(isStart) {

                    viewModel.startLocationStart.value?.let {
                        val cu = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                        googleMap?.moveCamera(cu)
                        if(isLocationFailedBefore) {
                            isLocationFailedBefore = false
                        }
                        else {
                            return
                        }
                    }
                }
            }
            1 -> {
                if(isStart) {

                    viewModel.startLocationReport.value?.let {
                        val cu = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                        googleMap?.moveCamera(cu)
                        if(isLocationFailedBefore) {
                            isLocationFailedBefore = false
                        }
                        else {
                            return
                        }
                    }
                }
                else {
                    viewModel.endLocationReport.value?.let {
                        val cu = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                        googleMap?.moveCamera(cu)
                        if(isLocationFailedBefore) {
                            isLocationFailedBefore = false
                        }
                        else {
                            return
                        }
                    }
                }
            }
            2 -> {
                if(isStart.not()) {

                    viewModel.endLocationFinish.value?.let {
                        val cu = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                        googleMap?.moveCamera(cu)
                        if(isLocationFailedBefore) {
                            isLocationFailedBefore = false
                        }
                        else {
                            return
                        }
                    }
                }
            }
        }

        /*val cu = CameraUpdateFactory.newLatLngZoom(LatLng(41.110196, 29.024381), 16f)
        googleMap.moveCamera(cu)*/


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

                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 16f)
                googleMap?.moveCamera(cu)

            }

            override fun onLocationFailed(message: String) {

                if(activity?.isFinishing != false || activity?.isDestroyed != false) {
                    return
                }

                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(41.110196, 29.024381), 16f)
                googleMap?.moveCamera(cu)

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
        isLocationFailedBefore = true
        val cu = CameraUpdateFactory.newLatLngZoom(LatLng(41.110196, 29.024381), 16f)
        googleMap?.moveCamera(cu)
    }


}