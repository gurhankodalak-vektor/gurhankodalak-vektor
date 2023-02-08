package com.vektortelekom.android.vservice.ui.route

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.RouteSelectionActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.shuttle.bottomsheet.BottomSheetRoutePreview
import javax.inject.Inject

class RouteSelectionActivity : BaseActivity<ShuttleViewModel>() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    private lateinit var binding: RouteSelectionActivityBinding

    private lateinit var bottomSheetBehaviorEditShuttle: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<RouteSelectionActivityBinding>(this, R.layout.route_selection_activity).apply {
            lifecycleOwner = this@RouteSelectionActivity
        }


        viewModel.isLocationToHome.value = true
        viewModel.isComingSurvey = true
        viewModel.isFromAddressSelect = intent.getBooleanExtra("isFromAddressSelect", false)

        bottomSheetBehaviorEditShuttle = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehaviorEditShuttle.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_HALF_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN

                    finish()
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })

        if(savedInstanceState == null){
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.root_fragment, RouteSelectionFragment.newInstance(), RouteSelectionFragment.TAG)
                    .addToBackStack(null)
                    .commit()
        }

        viewModel.openRouteSelection.observe(this) {
            if (it != null) {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.root_fragment, RouteSelectionFragment.newInstance(), RouteSelectionFragment.TAG)
                        .commit()

                viewModel.openRouteSelection.value = null
            }
        }

        viewModel.openBottomSheetRoutePreview.observe(this) {
            if (it != null) {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.root_fragment, BottomSheetRoutePreview.newInstance(), BottomSheetRoutePreview.TAG)
                        .commit()

                viewModel.openBottomSheetRoutePreview.value = null
            }
        }

        viewModel.openStopSelection.observe(this) {
            if (it != null) {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.bottom_root_view, StopSelectionFragment.newInstance(), StopSelectionFragment.TAG)
                        .commit()

                viewModel.openStopSelection.value = null

                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        binding.buttonSelectCancel.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
        }

        viewModel.routeSelectedForReservation.observe(this) {
            viewModel.openRouteSelection.value = true
        }

        viewModel.openNumberPicker.observe(this) { currentSelection ->
            if (currentSelection != null) {

                val displayedValues: Array<String> = when (currentSelection) {
                    ShuttleViewModel.SelectType.RouteSorting -> {
                        val values = Array(viewModel.routeSortList.size) { "" }
                        binding.textViewSelectTitle.text = getString(R.string.sorting)

                        for (i in viewModel.routeSortList.indices) {

                            val name = when (viewModel.routeSortList[i]) {
                                ShuttleViewModel.RouteSortType.WalkingDistance -> getString(R.string.walking_distance)
                                ShuttleViewModel.RouteSortType.TripDuration -> getString(R.string.trip_duration)
                                ShuttleViewModel.RouteSortType.OccupancyRatio -> getString(R.string.occupancy_ratio)

                            }

                            values[i] = name
                        }

                        values
                    }
                    else -> {
                        val values = Array(viewModel.routeSortList.size) { "" }
                        values
                    }

                }

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
        }

        binding.buttonSelectSelect.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
            viewModel.openNumberPicker.value?.let { currentSelect ->
                when(currentSelect) {
                    ShuttleViewModel.SelectType.RouteSorting -> {

                        viewModel.selectedRouteSortItemIndexTrigger.value = binding.numberPicker.value

                        viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_EXPANDED
                    }
                    else -> {

                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = ViewModelProvider(this, factory)[ShuttleViewModel::class.java]
        return viewModel
    }



}