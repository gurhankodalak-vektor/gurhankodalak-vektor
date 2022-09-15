package com.vektortelekom.android.vservice.ui.shuttle.bottomsheet

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.RouteModel
import com.vektortelekom.android.vservice.databinding.BottomSheetFromWhereBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.shuttle.adapter.SearchFromToPlaceResultsAdapter
import javax.inject.Inject

class BottomSheetFromWhere : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: BottomSheetFromWhereBinding

    private lateinit var placesClient: PlacesClient

    private lateinit var searchPlacesResultsAdapter: SearchFromToPlaceResultsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<BottomSheetFromWhereBinding>(inflater, R.layout.bottom_sheet_from_where, container, false).apply {
            lifecycleOwner = this@BottomSheetFromWhere
            viewModel = this@BottomSheetFromWhere.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placesClient = Places.createClient(requireContext())

        binding.bottomSheetFromWhereImageViewClose.setOnClickListener {

            viewModel.isReturningShuttleEdit = true
            viewModel.openBottomSheetEditShuttle.value = true

        }

        binding.layoutMyLocation.setOnClickListener {
            if(viewModel.myLocation == null) {
                Toast.makeText(requireContext(), "Konum henüz alınamadı", Toast.LENGTH_SHORT).show()
            }
            else {

                if(viewModel.currentRide?.fromTerminalReferenceId == null) {
                    viewModel.isFromChanged = true
                    viewModel.selectedFromLocation = ShuttleViewModel.FromToLocation(
                            location = viewModel.myLocation!!,
                            text = getString(R.string.my_location),
                            destinationId = null
                    )
                    viewModel.textViewBottomSheetEditShuttleRouteFrom.value = viewModel.selectedFromLocation?.text
                }
                else {
                    viewModel.isToChanged = true
                    viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                            location = viewModel.myLocation!!,
                            text = getString(R.string.my_location),
                            destinationId = null
                    )
                    viewModel.textViewBottomSheetEditShuttleRouteTo.value = viewModel.selectedToLocation?.text
                }

                viewModel.isReturningShuttleEdit = true
                viewModel.openBottomSheetEditShuttle.value = true

            }
        }

        binding.editTextAddressSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                viewModel.waitingForSearchResponse = true
                //viewModel.searchRoute(binding.editTextAddressSearch.text.toString())

                val token = AutocompleteSessionToken.newInstance()
                val request = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(binding.editTextAddressSearch.text.toString())
                        .build()


                placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->

                    viewModel.autocompletePredictions.value = response.autocompletePredictions

                    (requireActivity() as BaseActivity<*>).dismissPd()
                    viewModel.setIsLoading(false)

                    viewModel.setSearchListAdapter.value = true

                }
                        .addOnFailureListener {
                            (requireActivity() as BaseActivity<*>).dismissPd()
                        }
            }

            true
        }

        binding.layoutHome.setOnClickListener {

            if(viewModel.isShuttleServicePlaningFragment){

                viewModel.selectedDate?.let { selectedDate ->
                    val homeLocationModel = AppDataManager.instance.personnelInfo?.homeLocation

                    val homeLocation = Location("")
                    homeLocation.latitude = homeLocationModel?.latitude ?: 0.0
                    homeLocation.longitude = homeLocationModel?.longitude ?: 0.0

                    if (selectedDate.fromTerminalReferenceId == null) {
                        viewModel.selectedFromLocation = ShuttleViewModel.FromToLocation(
                            location = homeLocation,
                            text = getString(R.string.shuttle_home),
                            destinationId = null
                        )
                        viewModel.selectedToLocation = null
                        viewModel.textViewBottomSheetEditShuttleRouteFrom.value = viewModel.selectedFromLocation?.text
                    } else {
                        viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                            location = homeLocation,
                            text = getString(R.string.shuttle_home),
                            destinationId = null
                        )
                        viewModel.selectedFromLocation = null
                        viewModel.textViewBottomSheetEditShuttleRouteTo.value = viewModel.selectedToLocation?.text
                    }

                    viewModel.isReturningShuttlePlanningEdit = true
                    viewModel.isReturningShuttleEdit = true
                    viewModel.openBottomSheetEditShuttle.value = true
                }

            } else{
                val homeLocationModel = AppDataManager.instance.personnelInfo?.homeLocation

                val homeLocation = Location("")
                homeLocation.latitude = homeLocationModel?.latitude?:0.0
                homeLocation.longitude = homeLocationModel?.longitude?:0.0

                if(viewModel.currentRide?.fromTerminalReferenceId == null) {
                    viewModel.isFromChanged = true
                    viewModel.selectedFromLocation = ShuttleViewModel.FromToLocation(
                        location = homeLocation,
                        text = getString(R.string.home_location),
                        destinationId = null
                    )
                    viewModel.textViewBottomSheetEditShuttleRouteFrom.value = viewModel.selectedFromLocation?.text
                }
                else {
                    viewModel.isToChanged = true
                    viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                        location = homeLocation,
                        text = getString(R.string.home_location),
                        destinationId = null
                    )
                    viewModel.textViewBottomSheetEditShuttleRouteTo.value = viewModel.selectedToLocation?.text
                }
                viewModel.isReturningShuttleEdit = true
                viewModel.openBottomSheetEditShuttle.value = true
            }

        }

        viewModel.setSearchListAdapter.observe(viewLifecycleOwner) {
            if(it!= null) {
                setSearchListAdapter()

                viewModel.setSearchListAdapter.value = null
            }
        }

    }

    private fun setSearchListAdapter() {

        binding.cardViewSearchResults.visibility = View.VISIBLE

        searchPlacesResultsAdapter = SearchFromToPlaceResultsAdapter(viewModel.autocompletePredictions.value?: listOf(), viewModel.searchRouteResponse.value?: listOf(), object : SearchFromToPlaceResultsAdapter.SearchItemClickListener {

            override fun onItemClicked(autocompletePrediction: AutocompletePrediction) {

                binding.cardViewSearchResults.visibility = View.GONE
                (requireActivity() as BaseActivity<*>).showPd()
                placesClient.fetchPlace(FetchPlaceRequest.builder(autocompletePrediction.placeId, listOf(Place.Field.LAT_LNG, Place.Field.NAME))
                        .setSessionToken(AutocompleteSessionToken.newInstance())
                        .build())
                        .addOnSuccessListener { response ->
                            (requireActivity() as BaseActivity<*>).dismissPd()

                            val location = Location("")
                            location.latitude = response.place.latLng?.latitude?:0.0
                            location.longitude = response.place.latLng?.longitude?:0.0

                            if (viewModel.isShuttleServicePlaningFragment){
                                if(viewModel.workgroupTemplate?.fromTerminalReferenceId == null) {
                                    viewModel.isFromChanged = true
                                    viewModel.selectedFromLocation = ShuttleViewModel.FromToLocation(
                                            location = location,
                                            text = response.place.name?:"",
                                            destinationId = null
                                    )
                                    viewModel.textViewBottomSheetEditShuttleRouteFrom.value = viewModel.selectedFromLocation?.text
                                }
                                else {
                                    viewModel.isToChanged = true
                                    viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                                            location = location,
                                            text = response.place.name?:"",
                                            destinationId = null
                                    )
                                    viewModel.textViewBottomSheetEditShuttleRouteTo.value = viewModel.selectedToLocation?.text
                                }
                            } else{
                                if(viewModel.currentRide?.fromTerminalReferenceId == null) {
                                    viewModel.isFromChanged = true
                                    viewModel.selectedFromLocation = ShuttleViewModel.FromToLocation(
                                            location = location,
                                            text = response.place.name?:"",
                                            destinationId = null
                                    )
                                    viewModel.textViewBottomSheetEditShuttleRouteFrom.value = viewModel.selectedFromLocation?.text
                                }
                                else {
                                    viewModel.isToChanged = true
                                    viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                                            location = location,
                                            text = response.place.name?:"",
                                            destinationId = null
                                    )
                                    viewModel.textViewBottomSheetEditShuttleRouteTo.value = viewModel.selectedToLocation?.text
                                }
                            }


                            viewModel.isReturningShuttleEdit = true

                            viewModel.isReturningShuttlePlanningEdit = true
                            viewModel.openBottomSheetEditShuttle.value = true

                        }
                        .addOnFailureListener {
                            (requireActivity() as BaseActivity<*>).dismissPd()
                            viewModel.navigator?.handleError(it)
                        }

            }

            override fun onItemClicked(route: RouteModel) {
                viewModel.getRouteDetailsById(route.id)
                viewModel.navigator?.showShuttleMainFragment()
            }


        })

        binding.recyclerViewSearchResults.adapter = searchPlacesResultsAdapter
    }


    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "BottomSheetFromWhere"

        fun newInstance() = BottomSheetFromWhere()

    }

}