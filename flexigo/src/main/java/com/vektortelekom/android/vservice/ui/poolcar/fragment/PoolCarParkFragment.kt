package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.StationVehicleModel
import com.vektortelekom.android.vservice.databinding.PoolCarParkFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.ui.poolcar.adapter.ParkingCarModelsAdapter
import com.vektortelekom.android.vservice.utils.convertNowToTotalMinutesOfDay
import com.vektortelekom.android.vservice.utils.dpToPx
import javax.inject.Inject

class PoolCarParkFragment: BaseFragment<PoolCarViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    private lateinit var binding: PoolCarParkFragmentBinding

    private val isTelematics = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarParkFragmentBinding>(inflater, R.layout.pool_car_park_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarParkFragment
            viewModel = this@PoolCarParkFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedStation.observe(viewLifecycleOwner, Observer { park ->

            if(park == null) {
                return@Observer
            }

            binding.textViewParkingLotsName.text = park.name
            binding.textViewParkingLotsAddress.text = park.address

            park.workingHours?.let {
                calculateAndDrawOpeningStatus(it)
            }

            binding.textViewOpeningHours.text = park.workingHours

            viewModel.getStationVehicles(park.id)

        })

        viewModel.stationVehicles.observe(viewLifecycleOwner, Observer { vehicles ->

            if(vehicles == null) {
                return@Observer
            }

            val poolCarVehicles = mutableListOf<StationVehicleModel>()

            for(vehicle in vehicles) {
                poolCarVehicles.add(vehicle)
            }

            binding.recyclerViewCarModels.adapter = ParkingCarModelsAdapter(poolCarVehicles, object: ParkingCarModelsAdapter.CarModelListener {
                override fun carModelClicked(carModel: StationVehicleModel) {
                    viewModel.selectedVehicle.value = carModel.vehicle
                    viewModel.selectedVehicleImageUuid = carModel.imageUuid
                    viewModel.navigator?.showPoolCarVehicleFragment(null)
                }

            })

            binding.recyclerViewCarModels.addItemDecoration(object: RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    super.getItemOffsets(outRect, view, parent, state)

                    with(outRect) {
                        left = if (parent.getChildAdapterPosition(view) == 0) {
                            0f.dpToPx(requireContext())
                        } else {
                            10f.dpToPx(requireContext())
                        }
                        right = if(parent.getChildAdapterPosition(view) == poolCarVehicles.size-1) {
                            0f.dpToPx(requireContext())
                        }
                        else {
                            10f.dpToPx(requireContext())
                        }
                    }
                }
            })

        })

        binding.textViewStationInfo1.visibility = if(isTelematics) View.VISIBLE else View.GONE
        binding.textViewStationInfo2.visibility = if(isTelematics) View.VISIBLE else View.GONE

    }

    private fun calculateAndDrawOpeningStatus(status: String) {
        val hours = status.split("-")

        if(hours.size == 2) {
            val startingTimeString = hours[0]
            val endingTimeString = hours[1]

            val startingTimes = startingTimeString.split(":")
            val startingMinutes = startingTimes[0].toInt()*60 + startingTimes[1].toInt()

            val endingTimes = endingTimeString.split(":")
            val endingMinutes = endingTimes[0].toInt()*60 + endingTimes[1].toInt()

            val currentMinutes = System.currentTimeMillis().convertNowToTotalMinutesOfDay()

            if(currentMinutes in startingMinutes..endingMinutes) {
                binding.imageViewOpeningStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                binding.textViewOpeningStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                binding.textViewOpeningStatus.text = getString(R.string.currently_open)
            }
            else {
                binding.imageViewOpeningStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.watermelon))
                binding.textViewOpeningStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.watermelon))
                binding.textViewOpeningStatus.text = getString(R.string.closed)
            }
        }
    }

    override fun getViewModel(): PoolCarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarParkFragment"

        fun newInstance() = PoolCarParkFragment()

    }

}