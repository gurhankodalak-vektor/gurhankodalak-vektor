package com.vektortelekom.android.vservice.ui.shuttle.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.RouteModel
import com.vektortelekom.android.vservice.databinding.BottomSheetSearchRouteBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.shuttle.adapter.SearchFromToPlaceResultsAdapter
import javax.inject.Inject

class BottomSheetSearchRoute : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: BottomSheetSearchRouteBinding

    private lateinit var searchPlacesResultsAdapter: SearchFromToPlaceResultsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<BottomSheetSearchRouteBinding>(inflater, R.layout.bottom_sheet_search_route, container, false).apply {
            lifecycleOwner = this@BottomSheetSearchRoute
            viewModel = this@BottomSheetSearchRoute.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bottomSheetSearchRouteImageViewClose.setOnClickListener {
            viewModel.isReturningShuttleEdit = true
            viewModel.openBottomSheetEditShuttle.value = true
        }


        binding.editTextRouteSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.searchRoute(v.text.toString())
            }

            true
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

            }

            override fun onItemClicked(route: RouteModel) {
                viewModel.fromPage = "BottomSheetSearchRoute"
                viewModel.getRouteDetailsByIdSearchRoute(route.id)
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
        const val TAG: String = "BottomSheetSearchRoute"

        fun newInstance() = BottomSheetSearchRoute()

    }

}