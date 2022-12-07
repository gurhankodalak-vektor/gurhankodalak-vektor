package com.vektortelekom.android.vservice.ui.route.search

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.FromToType
import com.vektortelekom.android.vservice.databinding.RouteSearchActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.route.RouteNavigator
import com.vektortelekom.android.vservice.ui.route.bottomsheet.BottomSheetRouteSearchLocation
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.utils.convertToShuttleDateTime
import java.util.*
import javax.inject.Inject

class RouteSearchActivity : BaseActivity<RouteSearchViewModel>(), RouteNavigator {
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RouteSearchViewModel

    private lateinit var binding: RouteSearchActivityBinding

    private lateinit var bottomSheetBehaviorEditShuttle: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<RouteSearchActivityBinding>(
            this,
            R.layout.route_search_activity
        ).apply {
            lifecycleOwner = this@RouteSearchActivity
        }

        viewModel.navigator = this

        bottomSheetBehaviorEditShuttle = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehaviorEditShuttle.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })

        viewModel.isLocationToHome.value = true
        viewModel.isFromEditPage.value = false
        viewModel.isFromChanged.value = false

        binding.buttonSelectCancel.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
            viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_HIDDEN
        }

        viewModel.bottomSheetBehaviorEditShuttleState.observe(this) {
            if (it != null) {
                bottomSheetBehaviorEditShuttle.state = it
                viewModel.bottomSheetBehaviorEditShuttleState.value = null
            }
        }

        binding.buttonSelectSelect.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE

            viewModel.openNumberPicker.value?.let { currentSelect ->
                when (currentSelect) {
                    RouteSearchViewModel.SelectType.Time -> {
                        viewModel.selectedDate = viewModel.dateAndWorkgroupList?.get(binding.numberPicker.value)
                        viewModel.selectedShiftIndex = viewModel.dateAndWorkgroupList?.get(binding.numberPicker.value)?.workgroupIndex!!
                        viewModel.selectedDateIndex = binding.numberPicker.value

                        viewModel.isSelectedTime.value = true
                        viewModel.currentWorkgroup.value = viewModel.allWorkgroup.value?.get(viewModel.selectedShiftIndex)

                        viewModel.getWorkgroupInformation(viewModel.currentWorkgroup.value!!.workgroupInstanceId)

                    }
                    RouteSearchViewModel.SelectType.RouteSorting -> {

                    viewModel.selectedRouteSortItemIndexTrigger.value = binding.numberPicker.value

                    viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_EXPANDED
                    }
                    RouteSearchViewModel.SelectType.CampusFrom -> {
                        viewModel.selectedFromDestination = viewModel.destinations.value?.get(binding.numberPicker.value)
                        viewModel.selectedFromDestinationIndex = binding.numberPicker.value
                        viewModel.fromLabelText.value = viewModel.destinations.value?.get(binding.numberPicker.value)?.title
                        viewModel.fromLocation.value = viewModel.selectedFromDestination?.location

                        viewModel.destinationId = viewModel.selectedFromDestination!!.id

                        viewModel.isFromEditPage.value = true
                        viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_HIDDEN
                    }
                }
            }
        }

        viewModel.openNumberPicker.observe(this, Observer { currentSelection ->
            if (currentSelection != null) {
                var tempList : Array<Long>? = null
                val displayedValues: Array<String> = when (currentSelection) {
                    RouteSearchViewModel.SelectType.Time -> {
                        binding.textViewSelectTitle.text = viewModel.pickerTitle

                        val values: Array<String>

                        if (viewModel.dateAndWorkgroupList != null) {
                            values = Array(viewModel.dateAndWorkgroupList!!.size) { "" }
                            tempList = Array(viewModel.dateAndWorkgroupList!!.size) { 0L }

                            for (i in viewModel.dateAndWorkgroupList!!.indices) {
                                values[i] = viewModel.dateAndWorkgroupList!![i].ride.firstDepartureDate.convertToShuttleDateTime()
                                tempList[i] = viewModel.dateAndWorkgroupList!![i].ride.firstDepartureDate
                            }
                        } else {
                            return@Observer
                        }

                        values

                    }
                    RouteSearchViewModel.SelectType.RouteSorting -> {
                        val values = Array(viewModel.routeSortList.size) { "" }
                        binding.textViewSelectTitle.text = getString(R.string.sorting)

                        for (i in viewModel.routeSortList.indices) {

                            val name = when (viewModel.routeSortList[i]) {
                                ShuttleViewModel.RouteSortType.WalkingDistance -> {
                                    getString(R.string.walking_distance)
                                }
                                ShuttleViewModel.RouteSortType.TripDuration -> {
                                    getString(R.string.trip_duration)
                                }
                                ShuttleViewModel.RouteSortType.OccupancyRatio -> {
                                    getString(R.string.occupancy_ratio)
                                }
                            }

                            values[i] = name
                        }

                        values
                    }
                    RouteSearchViewModel.SelectType.CampusFrom -> {
                        val destinations = viewModel.destinations.value
                        binding.textViewSelectTitle.text = getString(R.string.campus)

                        val values: Array<String>

                        if (destinations != null) {
                            values = Array(destinations.size) { "" }
                            for (i in destinations.indices) {
                                values[i] = destinations[i].title ?: destinations[i].name ?: ""
                            }
                        } else {
                            return@Observer
                        }

                        values

                    }
                }

                val currentTime = Calendar.getInstance().time.time
                val closest = tempList?.toList()?.let {
                    Collections.min(
                        it
                    ) { p0, p1 ->
                        val diff1: Long = Math.abs(
                            p0?.minus(currentTime) ?: 0
                        )
                        val diff2: Long = Math.abs(
                            p1?.minus(currentTime) ?: 0
                        )
                        if (diff1 < diff2) -1 else 1
                    }
                }

                if (closest != null) {
                    viewModel.selectedRouteSortItemIndex = tempList?.toList()?.indexOf(closest)
                }

                if (viewModel.isSelectedTime.value == true)
                    viewModel.selectedRouteSortItemIndex = viewModel.selectedDateIndex
                else
                    viewModel.selectedRouteSortItemIndex = tempList?.toList()?.indexOf(closest)


                binding.numberPicker.value = 0
                binding.numberPicker.displayedValues = null
                binding.numberPicker.minValue = 0
                binding.numberPicker.maxValue = displayedValues.size - 1
                binding.numberPicker.displayedValues = displayedValues
                binding.numberPicker.value = viewModel.selectedRouteSortItemIndex ?: 0
                binding.numberPicker.wrapSelectorWheel = false


                binding.layoutSelect.visibility = View.VISIBLE

                viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_HIDDEN

            }
        })

        viewModel.openBottomSheetSearchLocation.observe(this) {
            if (it != null) {

                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.bottom_root_view,
                        BottomSheetRouteSearchLocation.newInstance(),
                        BottomSheetRouteSearchLocation.TAG
                    )
                    .commit()

                viewModel.openBottomSheetSearchLocation.value = null
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun getViewModel(): RouteSearchViewModel {
        viewModel = ViewModelProvider(this, factory)[RouteSearchViewModel::class.java]
        return viewModel
    }



}