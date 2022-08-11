package com.vektortelekom.android.vservice.ui.poolcar.reservation.fragment

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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
import com.vektortelekom.android.vservice.databinding.PoolCarReservationSelectToFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.menu.adapters.SearchPlaceResultsAdapter
import com.vektortelekom.android.vservice.ui.poolcar.reservation.PoolCarReservationViewModel
import com.vektortelekom.android.vservice.utils.AutoCompleteManager
import com.vektortelekom.android.vservice.utils.bitmapDescriptorFromVector
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class PoolCarReservationSelectToFragment: BaseFragment<PoolCarReservationViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarReservationViewModel

    private lateinit var binding: PoolCarReservationSelectToFragmentBinding

    private var googleMap: GoogleMap? = null

    @Volatile
    private var myLocation: Location? = null
    private lateinit var locationClient: FusedLocationClient

    private lateinit var placesClient: PlacesClient

    private lateinit var searchPlacesResultsAdapter: SearchPlaceResultsAdapter

    private var homeIcon: BitmapDescriptor? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarReservationSelectToFragmentBinding>(inflater, R.layout.pool_car_reservation_select_to_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarReservationSelectToFragment
            viewModel = this@PoolCarReservationSelectToFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                            val cu = CameraUpdateFactory.newLatLng(response.place.latLng!!)
                            googleMap?.moveCamera(cu)
                            viewModel.reservationToLocationTemp.value = response.place.latLng
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
            homeIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_home)

            continueAfterMapInitialized()

            googleMap.setOnCameraIdleListener {

                viewModel.reservationToLocationTemp.value = googleMap.cameraPosition.target

                val geoCoder = Geocoder(requireContext(), Locale("tr-TR"))

                try{
                    val addresses = geoCoder.getFromLocation(googleMap.cameraPosition.target.latitude, googleMap.cameraPosition.target.longitude, 1)

                    if(addresses.size > 0) {
                        val address = addresses[0]
                        viewModel.reservationAddressTextToTemp.value = address.getAddressLine(0)
                        binding.editTextResult.setText(address.getAddressLine(0))
                    }
                }
                catch (e: Exception) {
                    binding.editTextResult.setText("")
                }


            }

            val homeLocation = AppDataManager.instance.personnelInfo?.homeLocation

            if(homeLocation != null) {
                googleMap.addMarker(MarkerOptions().position(LatLng(homeLocation.latitude, homeLocation.longitude)).icon(homeIcon))
            }

        }

        /*viewModel.homeLocationSuccess.observe(viewLifecycleOwner, androidx.lifecycle.Observer { result ->

            if(result != null) {

                val dialog = AppDialog.Builder(requireContext())
                        .setIconVisibility(true)
                        .setTitle(R.string.dialog_message_change_address_success)
                        .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                            if(viewModel.isAddressNotValid.value == true) {
                                activity?.finish()
                            }
                            else {
                                viewModel.navigator?.returnMenuMainFragment()
                            }
                        }
                        .create()

                dialog.show()

                viewModel.homeLocationSuccess.value = null
            }


        })*/

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


    override fun getViewModel(): PoolCarReservationViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarReservationViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarReservationSelectToFragment"

        fun newInstance() = PoolCarReservationSelectToFragment()

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
                AppDataManager.instance.currentLocation = location
                googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                googleMap?.isMyLocationEnabled = true


                binding.buttonMyLocation.visibility = View.VISIBLE

                myLocation = location

                locationClient.stop()

                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 14f)
                googleMap?.moveCamera(cu)

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