package com.vektortelekom.android.vservice.ui.shuttle.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.DestinationModel
import com.vektortelekom.android.vservice.data.model.RouteModel
import com.vektortelekom.android.vservice.databinding.ShuttleRouteSearchFromFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.shuttle.adapter.DestinationListAdapter
import com.vektortelekom.android.vservice.ui.shuttle.adapter.SearchFromToPlaceResultsAdapter
import com.vektortelekom.android.vservice.ui.shuttle.model.VPlaceModel
import com.vektortelekom.android.vservice.utils.AutoCompleteManager
import javax.inject.Inject

class ShuttleRouteSearchFromToFragment : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    private lateinit var binding: ShuttleRouteSearchFromFragmentBinding

    private lateinit var placesClient: PlacesClient

    private lateinit var searchPlacesResultsAdapter: SearchFromToPlaceResultsAdapter



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<ShuttleRouteSearchFromFragmentBinding>(inflater, R.layout.shuttle_route_search_from_fragment, container, false).apply {
            lifecycleOwner = this@ShuttleRouteSearchFromToFragment
            viewModel = this@ShuttleRouteSearchFromToFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placesClient = Places.createClient(requireContext())

        when (viewModel.currentSearchType) {
            ShuttleViewModel.SearchType.from -> {
                binding.imageViewFromTo.setImageResource(R.drawable.ic_from)
                binding.textViewFromTo.setTextColor(ContextCompat.getColor(requireContext(), R.color.purpley))
                binding.textViewFromTo.text = getString(R.string.from)

                if(viewModel.toPlace.value != null) {
                    if(viewModel.toPlace.value?.isCampus == true) {
                        binding.layoutCampus.visibility = View.GONE
                    }
                    else {
                        binding.layoutHome.visibility = View.GONE
                        binding.layoutMyLocation.visibility = View.GONE
                        //binding.layoutWork.visibility = View.GONE
                        binding.editTextAddressSearch.isEnabled = false
                        binding.editTextAddressSearch.setText(R.string.select_a_destination)
                        viewModel.getDestinations()
                    }
                }


            }
            ShuttleViewModel.SearchType.to -> {
                binding.imageViewFromTo.setImageResource(R.drawable.ic_to)
                binding.textViewFromTo.setTextColor(ContextCompat.getColor(requireContext(), R.color.marigold))
                binding.textViewFromTo.text = getString(R.string.to)

                if(viewModel.fromPlace.value != null) {

                    if(viewModel.fromPlace.value != null && viewModel.fromPlace.value?.isCampus == true) {
                        binding.layoutCampus.visibility = View.GONE
                    }
                    else {
                        binding.layoutHome.visibility = View.GONE
                        binding.layoutMyLocation.visibility = View.GONE
                        //binding.layoutWork.visibility = View.GONE
                        binding.editTextAddressSearch.isEnabled = false
                        binding.editTextAddressSearch.setText(R.string.select_a_destination)
                        viewModel.getDestinations()
                    }

                }

            }
        }

        viewModel.searchRouteResponse.observe(viewLifecycleOwner) {

            if(viewModel.waitingForSearchResponse) {
                viewModel.waitingForSearchResponse = false
            }
            else {
                viewModel.setIsLoading(false)
                setSearchListAdapter()
            }

        }


        binding.editTextAddressSearch.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_NEXT) {

                viewModel.waitingForSearchResponse = true
                viewModel.searchRoute(binding.editTextAddressSearch.text.toString())

                val searchKey = binding.editTextAddressSearch.text.toString()
                val request = AutoCompleteManager.instance.getAutoCompleteRequest(searchKey, AppDataManager.instance.currentLocation)

                placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->

                    viewModel.autocompletePredictions.value = response.autocompletePredictions

                    if(viewModel.waitingForSearchResponse) {
                        viewModel.waitingForSearchResponse = false
                    }
                    else {
                        (requireActivity() as BaseActivity<*>).dismissPd()
                        viewModel.setIsLoading(false)

                        setSearchListAdapter()
                    }

                }
                        .addOnFailureListener {
                            (requireActivity() as BaseActivity<*>).dismissPd()
                        }
            }

            true
        }

        binding.layoutHome.setOnClickListener {
            when (viewModel.currentSearchType) {
                ShuttleViewModel.SearchType.from -> {
                    viewModel.fromPlace.value = VPlaceModel(getString(R.string.home), LatLng(AppDataManager.instance.personnelInfo?.homeLocation?.latitude?:0.0, AppDataManager.instance.personnelInfo?.homeLocation?.longitude?:0.0), false, null)
                }
                ShuttleViewModel.SearchType.to -> {
                    viewModel.toPlace.value = VPlaceModel(getString(R.string.home), LatLng(AppDataManager.instance.personnelInfo?.homeLocation?.latitude?:0.0, AppDataManager.instance.personnelInfo?.homeLocation?.longitude?:0.0), false, null)
                }
            }
            viewModel.navigator?.showFromToMapFragment(null)
        }

        binding.layoutMyLocation.setOnClickListener {

            if(viewModel.myLocation == null) {
                viewModel.navigator?.handleError(Exception(getString(R.string.pool_car_intercity_start_error_location)))
            }
            else {
                viewModel.myLocation?.let {
                    when (viewModel.currentSearchType) {
                        ShuttleViewModel.SearchType.from -> {
                            viewModel.fromPlace.value = VPlaceModel(getString(R.string.my_location), LatLng(it.latitude, it.longitude), false, null)
                        }
                        ShuttleViewModel.SearchType.to -> {
                            viewModel.toPlace.value = VPlaceModel(getString(R.string.my_location), LatLng(it.latitude, it.longitude), false, null)
                        }
                    }
                    viewModel.navigator?.showFromToMapFragment(null)
                }
            }
        }

        /*binding.layoutWork.setOnClickListener {
            when (viewModel.currentSearchType) {
                ShuttleViewModel.SearchType.from -> {
                    viewModel.fromPlace.value = VPlaceModel(getString(R.string.work), viewModel.workLocation?: LatLng(0.0, 0.0), false, null)
                }
                ShuttleViewModel.SearchType.to -> {
                    viewModel.toPlace.value = VPlaceModel(getString(R.string.work), viewModel.workLocation?: LatLng(0.0, 0.0), false, null)
                }
            }
            viewModel.navigator?.showFromToMapFragment(null)
        }*/

        binding.layoutCampus.setOnClickListener {
            viewModel.getDestinations()
        }

        viewModel.destinations.observe(viewLifecycleOwner, Observer { response ->

            if(response != null) {

                binding.cardViewSearchResults.visibility = View.VISIBLE
                binding.recyclerViewSearchResults.adapter = DestinationListAdapter(response, object: DestinationListAdapter.DestinationItemClickListener {
                    override fun onItemClicked(destination: DestinationModel) {
                        when (viewModel.currentSearchType) {
                            ShuttleViewModel.SearchType.from -> {
                                viewModel.fromPlace.value = VPlaceModel(destination.title?:destination.name?:"", LatLng(destination.location?.latitude?:0.0, destination.location?.longitude?:0.0), true, destination.id)
                            }
                            ShuttleViewModel.SearchType.to -> {
                                viewModel.toPlace.value = VPlaceModel(destination.title?:destination.name?:"", LatLng(destination.location?.latitude?:0.0, destination.location?.longitude?:0.0), true, destination.id)
                            }
                        }
                        viewModel.navigator?.showFromToMapFragment(null)
                    }

                })

                viewModel.destinations.value = null

            }

        })

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
                            when (viewModel.currentSearchType) {
                                ShuttleViewModel.SearchType.from -> {
                                    viewModel.fromPlace.value = VPlaceModel(response.place.name?:"", response.place.latLng?: LatLng(0.0, 0.0), false, null)
                                }
                                ShuttleViewModel.SearchType.to -> {
                                    viewModel.toPlace.value = VPlaceModel(response.place.name?:"", response.place.latLng?: LatLng(0.0, 0.0), false, null)
                                }
                            }
                            viewModel.navigator?.showFromToMapFragment(null)
                        }
                        .addOnFailureListener {
                            (requireActivity() as BaseActivity<*>).dismissPd()
                            viewModel.navigator?.handleError(it)
                        }

            }

            override fun onItemClicked(route: RouteModel) {
                viewModel.getRouteDetailsById(route.id)
                viewModel.navigator?.showShuttleMainFragment()
                viewModel.navigator?.changeTitle(getString(R.string.plan_service))
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
        const val TAG: String = "ShuttleRouteSearchFromToFragment"

        fun newInstance() = ShuttleRouteSearchFromToFragment()

    }

}