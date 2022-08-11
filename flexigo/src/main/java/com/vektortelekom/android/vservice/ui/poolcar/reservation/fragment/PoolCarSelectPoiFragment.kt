package com.vektortelekom.android.vservice.ui.poolcar.reservation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarSelectPoiFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.reservation.PoolCarReservationViewModel
import com.vektortelekom.android.vservice.ui.poolcar.reservation.adapter.PoiListAdapter
import java.util.*
import javax.inject.Inject

class PoolCarSelectPoiFragment : BaseFragment<PoolCarReservationViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarReservationViewModel

    private lateinit var binding: PoolCarSelectPoiFragmentBinding

    var parkingLotsAdapter: PoiListAdapter? = null
    var airportsAdapter: PoiListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarSelectPoiFragmentBinding>(inflater, R.layout.pool_car_select_poi_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarSelectPoiFragment
            viewModel = this@PoolCarSelectPoiFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewParkingLots.addItemDecoration(DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL))

        binding.recyclerViewAirports.addItemDecoration(DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL))

        parkingLotsAdapter = PoiListAdapter(viewModel.stations.value ?: listOf()) {
            if(viewModel.isSelectVehicleFromOrTo) {
                viewModel.selectedPoiFrom.value = it
            }
            else {
                viewModel.selectedPoiTo.value = it
            }

            viewModel.navigator?.backPressed(null)

        }

        airportsAdapter = PoiListAdapter(viewModel.poiList.value ?: listOf()) {
            if(viewModel.isSelectVehicleFromOrTo) {
                viewModel.selectedPoiFrom.value = it
            }
            else {
                viewModel.selectedPoiTo.value = it
            }

            viewModel.navigator?.backPressed(null)
        }

        binding.recyclerViewParkingLots.adapter = parkingLotsAdapter
        binding.recyclerViewAirports.adapter = airportsAdapter

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val stations = viewModel.stations.value?.filter { item -> item.name?.lowercase(Locale.ROOT)?.contains(it.lowercase(Locale.ROOT)) == true }
                            ?: listOf()
                    val airports = viewModel.poiList.value?.filter { item -> item.name.lowercase(Locale.ROOT).contains(it.lowercase(Locale.ROOT)) }
                            ?: listOf()

                    parkingLotsAdapter?.setList(stations)
                    airportsAdapter?.setList(airports)

                }
                return false

            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    val stations = viewModel.stations.value?.filter { item -> item.name?.lowercase(Locale.ROOT)?.contains(it.lowercase(Locale.ROOT)) == true }
                            ?: listOf()
                    val airports = viewModel.poiList.value?.filter { item -> item.name.lowercase(Locale.ROOT).contains(it.lowercase(Locale.ROOT)) }
                            ?: listOf()

                    parkingLotsAdapter?.setList(stations)
                    airportsAdapter?.setList(airports)

                }

                return false
            }

        })

    }

    override fun getViewModel(): PoolCarReservationViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarReservationViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarSelectPoiFragment"

        fun newInstance() = PoolCarSelectPoiFragment()

    }

}