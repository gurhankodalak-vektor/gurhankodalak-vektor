package com.vektortelekom.android.vservice.ui.route.bottomsheet

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.LocationModel
import com.vektortelekom.android.vservice.databinding.BottomSheetRouteSearchLocationBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.route.adapter.RouteAutoCompleteAdapter
import com.vektortelekom.android.vservice.ui.route.adapter.RouteLastSearchAdapter
import com.vektortelekom.android.vservice.ui.route.search.RouteSearchViewModel
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.utils.AutoCompleteManager
import java.util.*
import javax.inject.Inject

class BottomSheetRouteSearchLocation  : BaseFragment<RouteSearchViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RouteSearchViewModel

    private var tempList: ArrayList<LocationModel> = ArrayList()

    lateinit var binding: BottomSheetRouteSearchLocationBinding

    private lateinit var placesClient: PlacesClient

    private lateinit var routeLastSearchAdapter: RouteLastSearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<BottomSheetRouteSearchLocationBinding>(inflater, R.layout.bottom_sheet_route_search_location, container, false).apply {
            lifecycleOwner = this@BottomSheetRouteSearchLocation
            viewModel = this@BottomSheetRouteSearchLocation.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placesClient = Places.createClient(requireContext())

        binding.editTextAddressSearch.addTextChangedListener {
            if((it?.length ?: 0) > 2) {
                val searchKey = binding.editTextAddressSearch.text.toString()
                val request = AutoCompleteManager.instance.getAutoCompleteRequest(searchKey, AppDataManager.instance.currentLocation)

                placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                    viewModel.autocompletePredictions.value = response.autocompletePredictions
                    viewModel.setIsLoading(false)

                    setAutoCompleteList()

                }.addOnFailureListener {
                    (requireActivity() as BaseActivity<*>).dismissPd()
                }
            }

        }

        binding.layoutHome.setOnClickListener {
            viewModel.toLocation.value = AppDataManager.instance.personnelInfo?.homeLocation

            sendSearchPage(viewModel.toLocation.value?.latitude, viewModel.toLocation.value?.longitude, getString(R.string.home_location), false, true, true)

        }

        binding.layoutMyLocation.setOnClickListener {

            val myLocation = AppDataManager.instance.currentLocation
            if(myLocation == null) {
                Toast.makeText(requireContext(), getString(R.string.location_not_available), Toast.LENGTH_SHORT).show()
            } else {
                val geoCoder = Geocoder(requireContext(), Locale(resources.configuration.locale.language))

                try {
                    val addresses = myLocation.let { it1 -> geoCoder.getFromLocation(myLocation.latitude, it1.longitude, 1) }
                    if (addresses != null && addresses.size > 0) {
                        val address = addresses[0]

                        viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                            location = myLocation,
                            text = address.thoroughfare,
                            destinationId = null
                        )
                        viewModel.toLabelText.value = address.thoroughfare

                        val locationModel = LocationModel(0, myLocation.latitude, myLocation.longitude, address.getAddressLine(0), 0,0, true)
                        viewModel.toLocation.value = locationModel

                        viewModel.isLocationToHome.value = false
                        viewModel.isFromEditPage.value = true
                    }
                }
                catch (e: Exception) { }

                viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_HIDDEN

            }

        }

        binding.imageviewBack.setOnClickListener {
            viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_HIDDEN
        }

        if(AppDataManager.instance.lastRouteSearch?.isNotEmpty() == true)
            setLastSearchAdapter()

    }

    private fun sendSearchPage(latitude: Double?, longitude: Double?, address: String, isAddTempList: Boolean, isToHome: Boolean, isEditPage: Boolean ){
        val location = Location("")
        location.latitude = latitude ?: 0.0
        location.longitude = longitude ?: 0.0

        viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
            location = location,
            text = address,
            destinationId = null
        )
        viewModel.toLabelText.value = address

        val locationModel = LocationModel(0, latitude!!, longitude!!, address, 0,0, true)
        viewModel.toLocation.value = locationModel

        if (isAddTempList){
            if (AppDataManager.instance.lastRouteSearch != null)
                tempList = AppDataManager.instance.lastRouteSearch!!

            if (tempList.isNotEmpty() && tempList.size == 5)
                tempList.removeAt(0)

            if (!tempList.contains(locationModel))
                tempList.add(locationModel)

            AppDataManager.instance.lastRouteSearch = tempList
        }

        viewModel.isLocationToHome.value = isToHome
        viewModel.isFromEditPage.value = isEditPage

        viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_HIDDEN


    }

    private fun setAutoCompleteList(){

        val adapter = RouteAutoCompleteAdapter(requireContext(), R.layout.menu_add_address_search_view_holder, viewModel.autocompletePredictions.value?: listOf())
        binding.editTextAddressSearch.setAdapter(adapter)
        binding.editTextAddressSearch.threshold = 2

        binding.editTextAddressSearch.setOnItemClickListener { _, _, position, _ ->
            val item = adapter.getItem(position) as AutocompletePrediction?
            val address = item?.getPrimaryText(null).toString()

            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(binding.editTextAddressSearch.windowToken, 0)
            binding.editTextAddressSearch.setText("")

            placesClient.fetchPlace(FetchPlaceRequest.builder(item!!.placeId, listOf(Place.Field.LAT_LNG, Place.Field.NAME))
                .setSessionToken(AutocompleteSessionToken.newInstance())
                .build())
                .addOnSuccessListener { response ->

                    sendSearchPage(response.place.latLng!!.latitude, response.place.latLng!!.longitude, address, true, false, true)

                }
                .addOnFailureListener {
                    viewModel.navigator?.handleError(it)
                }
        }

    }
    private fun setLastSearchAdapter() {

        binding.recyclerviewLastSearch.visibility = View.VISIBLE

        routeLastSearchAdapter = RouteLastSearchAdapter(AppDataManager.instance.lastRouteSearch?: ArrayList(), object : RouteLastSearchAdapter.SearchItemClickListener {
            override fun onItemClicked(model: LocationModel) {
                binding.recyclerviewLastSearch.visibility = View.GONE

                sendSearchPage(model.latitude, model.longitude, model.address, false, false, true)

            }
        })

        binding.recyclerviewLastSearch.adapter = routeLastSearchAdapter
    }

    override fun getViewModel(): RouteSearchViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[RouteSearchViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "BottomSheetRouteSearchLocation"

        fun newInstance() = BottomSheetRouteSearchLocation()

    }

}