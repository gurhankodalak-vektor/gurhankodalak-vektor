package com.vektortelekom.android.vservice.ui.flexiride.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
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
import com.vektortelekom.android.vservice.databinding.FlexirideSearchFromFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.flexiride.FlexirideViewModel
import com.vektortelekom.android.vservice.ui.shuttle.adapter.DestinationListAdapter
import com.vektortelekom.android.vservice.ui.shuttle.adapter.SearchFromToPlaceResultsAdapter
import com.vektortelekom.android.vservice.utils.AutoCompleteManager
import javax.inject.Inject

class FlexirideSearchFromFragment : BaseFragment<FlexirideViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: FlexirideViewModel

    lateinit var binding: FlexirideSearchFromFragmentBinding

    private lateinit var placesClient: PlacesClient

    private lateinit var searchPlacesResultsAdapter: SearchFromToPlaceResultsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<FlexirideSearchFromFragmentBinding>(inflater, R.layout.flexiride_search_from_fragment, container, false).apply {
            lifecycleOwner = this@FlexirideSearchFromFragment
            viewModel = this@FlexirideSearchFromFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(viewModel.isFrom) {
            binding.textViewFromTo.text = getString(R.string.from)
            binding.textViewFromTo.setTextColor(ContextCompat.getColor(requireContext(), R.color.purpley))
            binding.imageViewFromTo.setImageResource(R.drawable.ic_from)
        }
        else {
            binding.textViewFromTo.text = getString(R.string.to)
            binding.textViewFromTo.setTextColor(ContextCompat.getColor(requireContext(), R.color.marigold))
            binding.imageViewFromTo.setImageResource(R.drawable.ic_to)
        }

        placesClient = Places.createClient(requireContext())


        binding.editTextAddressSearch.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {


                val searchKey = binding.editTextAddressSearch.text.toString()
                val request = AutoCompleteManager.instance.getAutoCompleteRequest(searchKey, AppDataManager.instance.currentLocation)

                placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->

                    viewModel.autocompletePredictions.value = response.autocompletePredictions

                    (requireActivity() as BaseActivity<*>).dismissPd()
                    viewModel.setIsLoading(false)

                    setSearchListAdapter()

                }
                .addOnFailureListener {
                        (requireActivity() as BaseActivity<*>).dismissPd()
                }
            }

            true
        }

        binding.layoutHome.setOnClickListener {

            if(viewModel.isFrom) {
                viewModel.fromLocation.value = LatLng(AppDataManager.instance.personnelInfo?.homeLocation?.latitude
                        ?: 0.0, AppDataManager.instance.personnelInfo?.homeLocation?.longitude
                        ?: 0.0)
            }
            else {
                viewModel.shouldCameraNavigateTo = true
                viewModel.toLocation.value = LatLng(AppDataManager.instance.personnelInfo?.homeLocation?.latitude
                        ?: 0.0, AppDataManager.instance.personnelInfo?.homeLocation?.longitude
                        ?: 0.0)
            }


            viewModel.navigator?.backPressed(null)
        }

        binding.layoutCampus.setOnClickListener {
            viewModel.getDestinations()
        }

        viewModel.destinations.observe(viewLifecycleOwner) { response ->
            if (response != null) {

                binding.cardViewSearchResults.visibility = View.VISIBLE
                binding.recyclerViewSearchResults.adapter = DestinationListAdapter(response, object : DestinationListAdapter.DestinationItemClickListener {
                    override fun onItemClicked(destination: DestinationModel) {
                        if (viewModel.isFrom) {
                            viewModel.fromLocation.value = LatLng(destination.location?.latitude
                                    ?: 0.0, destination.location?.longitude
                                    ?: 0.0)

                        } else {
                            viewModel.shouldCameraNavigateTo = true
                            viewModel.toLocation.value = LatLng(destination.location?.latitude
                                    ?: 0.0, destination.location?.longitude
                                    ?: 0.0)

                        }
                        viewModel.navigator?.backPressed(null)
                    }

                })

                viewModel.destinations.value = null

            }

        }

    }


    private fun setSearchListAdapter() {

        binding.cardViewSearchResults.visibility = View.VISIBLE

        searchPlacesResultsAdapter = SearchFromToPlaceResultsAdapter(viewModel.autocompletePredictions.value?: listOf(), listOf(), object : SearchFromToPlaceResultsAdapter.SearchItemClickListener {

            override fun onItemClicked(autocompletePrediction: AutocompletePrediction) {

                binding.cardViewSearchResults.visibility = View.GONE
                (requireActivity() as BaseActivity<*>).showPd()

                placesClient.fetchPlace(FetchPlaceRequest.builder(autocompletePrediction.placeId, listOf(Place.Field.LAT_LNG, Place.Field.NAME))
                        .setSessionToken(AutocompleteSessionToken.newInstance())
                        .build())
                        .addOnSuccessListener { response ->
                            (requireActivity() as BaseActivity<*>).dismissPd()

                            if(viewModel.isFrom) {
                                viewModel.fromLocation.value = response.place.latLng?: LatLng(0.0, 0.0)
                            }
                            else {
                                viewModel.shouldCameraNavigateTo = true
                                viewModel.toLocation.value = response.place.latLng ?: LatLng(0.0, 0.0)
                            }

                            viewModel.navigator?.backPressed(null)
                        }
                        .addOnFailureListener {
                            (requireActivity() as BaseActivity<*>).dismissPd()
                            viewModel.navigator?.handleError(it)
                        }

            }

            override fun onItemClicked(route: RouteModel) {

            }


        })

        binding.recyclerViewSearchResults.adapter = searchPlacesResultsAdapter
    }

    override fun getViewModel(): FlexirideViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[FlexirideViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "FlexirideSearchFromFragment"

        fun newInstance() = FlexirideSearchFromFragment()

    }

}