package com.vektortelekom.android.vservice.ui.shuttle

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.ShuttleActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.HighlightView
import com.vektortelekom.android.vservice.ui.comments.CommentsActivity
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.login.LoginActivity
import com.vektortelekom.android.vservice.ui.menu.MenuActivity
import com.vektortelekom.android.vservice.ui.route.RouteSelectionFragment
import com.vektortelekom.android.vservice.ui.route.bottomsheet.BottomSheetSelectRoutes
import com.vektortelekom.android.vservice.ui.route.search.RouteSearchActivity
import com.vektortelekom.android.vservice.ui.shuttle.bottomsheet.*
import com.vektortelekom.android.vservice.ui.shuttle.fragment.*
import com.vektortelekom.android.vservice.ui.vanpool.fragment.VanpoolDriverStationsFragment
import com.vektortelekom.android.vservice.ui.vanpool.fragment.VanpoolPassengerFragment
import com.vektortelekom.android.vservice.utils.*
import java.util.*
import javax.inject.Inject

class ShuttleActivity : BaseActivity<ShuttleViewModel>(), ShuttleNavigator,
    PermissionsUtils.CameraStateListener, PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    private lateinit var binding: ShuttleActivityBinding

    private var locationClient: FusedLocationClient? = null

    private lateinit var bottomSheetBehaviorEditShuttle: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ShuttleActivityBinding>(this, R.layout.shuttle_activity)
                .apply {
                    lifecycleOwner = this@ShuttleActivity
                    viewModel = this@ShuttleActivity.viewModel
                }
        viewModel.navigator = this
        viewModel.currentDay.value = Date().time.getDateWithZeroHour()

        if (AppDataManager.instance.personnelInfo == null) {
            viewModel.getPersonnelInfo()
        } else {
            continueAfterPersonnelInfo(savedInstanceState)
        }

        viewModel.personnelDetailsResponse.observe(this) {
            AppDataManager.instance.personnelInfo = it.response
            continueAfterPersonnelInfo(savedInstanceState)
        }

        binding.viewBackgroundBottomSheet.setOnClickListener {
            if (viewModel.isReturningShuttleEdit.not()) {
                viewModel.bottomSheetVisibility.value = false
                viewModel.searchedRoutes.value = null
                viewModel.searchedStops.value = null
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        binding.buttonSearch.setOnClickListener {
            val intent = Intent(this, RouteSearchActivity::class.java)
            startActivity(intent)
        }


        bottomSheetBehaviorEditShuttle = BottomSheetBehavior.from(binding.bottomSheetEditShuttle)
        bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehaviorEditShuttle.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if ((newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) && viewModel.isFromToShown.not()) {
                    if (viewModel.isReturningShuttleEdit.not()) {
                        viewModel.bottomSheetVisibility.value = false
                    } else {
                        viewModel.isReturningShuttleEdit = false
                    }
                }
                if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    viewModel.bottomSheetVisibility.value = false
                    viewModel.searchedRoutes.value = null
                    viewModel.searchedStops.value = null
                    bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })

        viewModel.isQrCodeOk.observe(this) { result ->
            if (result) {
                AppDialog.Builder(this@ShuttleActivity)
                    .setCloseButtonVisibility(false)
                    .setIconVisibility(false)
                    .setTitle(R.string.success)
                    .setSubtitle(R.string.shuttle_qr_success_message)
                    .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                        dialog.dismiss()
                    }.create().show()
            }
        }

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.bottom_root_view,
                BottomSheetEditShuttle.newInstance(),
                BottomSheetEditShuttle.TAG
            )
            .commit()

        viewModel.openNumberPicker.value = null

        viewModel.openNumberPicker.observe(this, Observer { currentSelection ->
            if (currentSelection != null) {

                val displayedValues: Array<String> = when (currentSelection) {
                    ShuttleViewModel.SelectType.CampusFrom -> {

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
                    ShuttleViewModel.SelectType.CampusTo -> {
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
                    ShuttleViewModel.SelectType.Time -> {
                        binding.textViewSelectTitle.text = getString(R.string.departure_time)
                        val values: Array<String>

                        if (viewModel.dateAndWorkgroupList != null) {
                            values = Array(viewModel.dateAndWorkgroupList!!.size) { "" }
                            for (i in viewModel.dateAndWorkgroupList!!.indices) {
                                // TODO: bu if ve else kısmından emin değilim. önceki hali comment halinde duruyor silmeyelim. gerekirse gürhana soralım
                                //if (viewModel.dateAndWorkgroupList!![i].ride.workgroupDirection == WorkgroupDirection.ONE_WAY)
                                //  {
                                values[i] = viewModel.dateAndWorkgroupList!![i].ride.firstDepartureDate.convertToShuttleDateTime()
                                //    }
                                /*   else{
                                       val departureHour = (viewModel.dateAndWorkgroupList!![i].ride.firstDepartureDate.convertToShuttleDateTime()
                                           ?: "")
                                       val returnDepartureHour = (viewModel.dateAndWorkgroupList!![i].ride.returnDepartureDate.convertToShuttleDateTime()
                                           ?: "")

                                       values[i] = "$departureHour-$returnDepartureHour"
                                   }*/


                                /* if (viewModel.dateAndWorkgroupList!![i].template?.direction == WorkgroupDirection.ONE_WAY)
                                     values[i] = viewModel.dateAndWorkgroupList!![i].date.convertToShuttleDateTime()
                                 else {
                                     val departureHour =
                                         ((viewModel.dateAndWorkgroupList!![i].template?.shift?.departureHour
                                             ?: viewModel.dateAndWorkgroupList!![i].template?.shift?.arrivalHour).convertHourMinutes()
                                             ?: "")
                                     val returnDepartureHour =
                                         ((viewModel.dateAndWorkgroupList!![i].template?.shift?.returnDepartureHour
                                             ?: viewModel.dateAndWorkgroupList!![i].template?.shift?.returnArrivalHour).convertHourMinutes()
                                             ?: "")

                                     values[i] = "$departureHour-$returnDepartureHour"

                                 }*/
                            }
                        } else {
                            return@Observer
                        }

                        values

                    }
                    ShuttleViewModel.SelectType.RouteSorting -> {
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
                }

                binding.numberPicker.value = 0
                binding.numberPicker.displayedValues = null
                binding.numberPicker.minValue = 0
                binding.numberPicker.maxValue = displayedValues.size - 1
                binding.numberPicker.displayedValues = displayedValues
                binding.numberPicker.value = viewModel.selectedRouteSortItemIndex ?: 0
                binding.numberPicker.wrapSelectorWheel = false

                viewModel.isReturningShuttleEdit = true
                if (viewModel.isShuttleServicePlaningFragment)
                    viewModel.isReturningShuttlePlanningEdit = true

                binding.layoutSelect.visibility = View.VISIBLE

                viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_HIDDEN

            }
        })

        binding.buttonSelectCancel.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
            viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.buttonSelectSelect.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
            viewModel.openNumberPicker.value?.let { currentSelect ->
                when (currentSelect) {
                    ShuttleViewModel.SelectType.CampusFrom -> {
                        viewModel.isFromChanged = true
                        viewModel.selectedFromDestination =
                            viewModel.destinations.value?.get(binding.numberPicker.value)
                        viewModel.selectedFromDestinationIndex = binding.numberPicker.value
                        viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
                            viewModel.selectedFromDestination?.title

                        viewModel.searchRoutesAdapterSetMyDestinationTrigger.value =
                            viewModel.selectedFromDestination!!

                        setDatesForEditShuttle(
                            destinationId = viewModel.selectedFromDestination!!.id,
                            fromType = FromToType.CAMPUS,
                            isFirstOpen = false,
                            isFromSelectDestination = true
                        )

                        viewModel.openBottomSheetEditShuttle.value = true
                    }
                    ShuttleViewModel.SelectType.CampusTo -> {
                        viewModel.isToChanged = true
                        viewModel.selectedToDestination =
                            viewModel.destinations.value?.get(binding.numberPicker.value)
                        viewModel.selectedToDestinationIndex = binding.numberPicker.value
                        viewModel.textViewBottomSheetEditShuttleRouteTo.value =
                            viewModel.selectedToDestination?.title
                        if (viewModel.isShuttleServicePlaningFragment) {
                            viewModel.selectedFromDestination = null
                            viewModel.selectedFromDestinationIndex = null
                        }
                        viewModel.searchRoutesAdapterSetMyDestinationTrigger.value =
                            viewModel.selectedToDestination

                        setDatesForEditShuttle(
                            destinationId = viewModel.selectedToDestination!!.id,
                            fromType = FromToType.PERSONNEL_SHUTTLE_STOP,
                            isFirstOpen = false,
                            isFromSelectDestination = true
                        )

                        viewModel.openBottomSheetEditShuttle.value = true
                    }
                    ShuttleViewModel.SelectType.Time -> {
                        viewModel.selectedDate =
                            viewModel.dateAndWorkgroupList?.get(binding.numberPicker.value)
                        viewModel.selectedDateIndex = binding.numberPicker.value

                        viewModel.dateAndWorkgroupList?.get(binding.numberPicker.value)?.template.let {
                            if (it != null) {
                                if (it.direction == WorkgroupDirection.ROUND_TRIP) {
                                    viewModel.textViewBottomSheetEditShuttleRouteTime.value =
                                        ((it.shift?.departureHour
                                            ?: it.shift?.arrivalHour).convertHourMinutes()
                                            ?: "") + "-" + ((it.shift?.returnDepartureHour
                                            ?: it.shift?.returnArrivalHour).convertHourMinutes()
                                            ?: "")
                                } else
                                    viewModel.textViewBottomSheetEditShuttleRouteTime.value =
                                        viewModel.selectedDate?.date.convertToShuttleDateTime()
                            }
                        }


                        if (viewModel.selectedToDestination != null) {
                            viewModel.selectedFromDestination = viewModel.selectedToDestination
                            viewModel.selectedFromDestinationIndex =
                                viewModel.selectedToDestinationIndex

                            viewModel.selectedToDestination = null
                            viewModel.selectedToDestinationIndex = null

                            viewModel.selectedToLocation = viewModel.selectedFromLocation
                            viewModel.selectedFromLocation = null

                            viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
                                viewModel.selectedFromDestination?.title
                            viewModel.textViewBottomSheetEditShuttleRouteTo.value =
                                viewModel.selectedToLocation?.text
                        }

                        if (viewModel.isShuttleServicePlaningFragment) {

                            viewModel.dateAndWorkgroupList?.forEach { list ->
                                if (viewModel.workgroupInstance?.name?.contains(list.ride.name) == true) {
                                    viewModel.workGroupSameNameList.value?.find {
                                        viewModel.workgroupInstance?.id == list.workgroupId
                                                && it.id == list.workgroupId
                                    }?.let {
                                        viewModel.workgroupInstance = it
                                    } ?: run {}
                                }
                            }

                        }
                        viewModel.openBottomSheetEditShuttle.value = true

                    }
                    ShuttleViewModel.SelectType.RouteSorting -> {

                        viewModel.selectedRouteSortItemIndexTrigger.value =
                            binding.numberPicker.value

                        viewModel.bottomSheetBehaviorEditShuttleState.value =
                            BottomSheetBehavior.STATE_EXPANDED
                    }
                    else -> {

                    }
                }
            }
        }

        viewModel.routeSelectedForReservation.observe(this) { route ->
            if (route != null) {
                var stop: StationModel? = route.closestStation

                if (route.closestStation?.name == null) {
                    val isFirstLeg = viewModel.currentRide?.workgroupDirection?.let {
                        viewModel.isFirstLeg(
                            it,
                            viewModel.currentRide?.fromType!!
                        )
                    } == true
                    if (route.getRoutePath(isFirstLeg)?.stations?.isNotEmpty() == true)
                        stop = route.getRoutePath(isFirstLeg)?.stations?.first()
                }

                viewModel.selectedRoute = route
                stop?.let {
                    viewModel.selectedStopForReservation = stop

                    viewModel.textViewBottomSheetStopName.value = stop.title
                    viewModel.textViewBottomSheetVehicleName.value = route.vehicle.plateId
                    viewModel.textViewBottomSheetRoutesTitle.value = route.title

                    viewModel.textViewBottomSheetReservationDate.value =
                        viewModel.selectedDate?.date.convertToShuttleReservationTime()

                    viewModel.isReturningShuttleEdit = true
                    viewModel.isMakeReservationOpening = true

                    viewModel.openBottomSheetMakeReservation.value = true


                    viewModel.selectedRoute?.let { routeModel ->
                        viewModel.selectedStation = stop
                        viewModel.zoomStation = true
                        viewModel.fillUITrigger.value = routeModel
                    }
                }

                viewModel.routeSelectedForReservation.value = null
            }
        }

        viewModel.openBottomSheetEditShuttle.observe(this) {
            if (it != null) {

                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.bottom_root_view,
                        BottomSheetEditShuttle.newInstance(),
                        BottomSheetEditShuttle.TAG
                    )
                    .commit()

                if (viewModel.isShuttleServicePlaningFragment && viewModel.isReturningShuttlePlanningEdit.not())
                    setBottomSheetData()

                viewModel.openBottomSheetEditShuttle.value = null
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        viewModel.openBottomSheetCalendar.observe(this) {
            if (it != null) {
                val bottomSheetCalendar = BottomSheetCalendar()
                bottomSheetCalendar.show(supportFragmentManager, bottomSheetCalendar.tag)

                viewModel.openBottomSheetCalendar.value = null

            }
        }

        viewModel.openBottomSheetSelectRoutes.observe(this) {
            if (it != null) {

                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.bottom_root_view,
                        BottomSheetSelectRoutes.newInstance(),
                        BottomSheetSelectRoutes.TAG
                    )
                    .commit()

                viewModel.openBottomSheetSelectRoutes.value = null
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        viewModel.openBottomSheetSearchRoute.observe(this) {
            if (it != null) {

                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.bottom_root_view,
                        BottomSheetSearchRoute.newInstance(),
                        BottomSheetSearchRoute.TAG
                    )
                    .commit()

                viewModel.openBottomSheetSearchRoute.value = null
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        viewModel.openBottomSheetRoutes.observe(this) {
            if (it != null) {

                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.bottom_root_view,
                        BottomSheetRoutes.newInstance(),
                        BottomSheetRoutes.TAG
                    )
                    .commit()

                if (bottomSheetBehaviorEditShuttle.state != BottomSheetBehavior.STATE_EXPANDED) {
                    viewModel.bottomSheetVisibility.value = true
                    bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_EXPANDED
                }
                viewModel.openBottomSheetRoutes.value = null
            }
        }

        viewModel.openBottomSheetRoutePreview.observe(this) {
            if (it != null) {

                supportFragmentManager
                    .beginTransaction()
                    .add(
                        R.id.bottom_root_view,
                        BottomSheetRoutePreview.newInstance(),
                        BottomSheetRoutePreview.TAG
                    )
                    .commit()

                viewModel.openBottomSheetRoutePreview.value = null
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        viewModel.openBottomSheetFromWhere.observe(this) {
            if (it != null) {

                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.bottom_root_view,
                        BottomSheetFromWhere.newInstance(),
                        BottomSheetFromWhere.TAG
                    )
                    .addToBackStack(null)
                    .commit()

                viewModel.openBottomSheetFromWhere.value = null
            }
        }

        viewModel.openBottomSheetMakeReservation.observe(this) {
            if (it != null) {

                supportFragmentManager
                    .beginTransaction()
                    .add(
                        R.id.bottom_root_view,
                        ShuttleServicePlanningReservationFragment.newInstance(),
                        ShuttleServicePlanningReservationFragment.TAG
                    )
                    .addToBackStack(null)
                    .commit()
                viewModel.openBottomSheetMakeReservation.value = null
            }
        }

        viewModel.openVanpoolDriverStations.observe(this) {
            if (it != null) {
                supportFragmentManager
                    .beginTransaction()
                    .add(
                        R.id.fragment_container_view,
                        VanpoolDriverStationsFragment.newInstance(),
                        VanpoolDriverStationsFragment.TAG
                    )
                    .addToBackStack(null)
                    .commit()
                viewModel.openBottomSheetMakeReservation.value = null
            }
        }

        viewModel.openVanpoolPassenger.observe(this) {
            if (it != null) {
                binding.textViewToolbarTitle.text =
                    viewModel.vanpoolPassengers.value?.size.toString().plus(" ")
                        .plus(getString(R.string.passengers))
                supportFragmentManager
                    .beginTransaction()
                    .add(
                        R.id.fragment_container_view,
                        VanpoolPassengerFragment.newInstance(),
                        VanpoolPassengerFragment.TAG
                    )
                    .addToBackStack(null)
                    .commit()
                viewModel.openVanpoolPassenger.value = null
            }
        }

        viewModel.updateShuttleDayResult.observe(this) {
            viewModel.bottomSheetVisibility.value = false
            bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN
            viewModel.searchedRoutes.value = null
            viewModel.searchedStops.value = null
            viewModel.getShuttleUseDays()
        }
        viewModel.openRouteSelection.observe(this) {
            if (it != null) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragment_container_view,
                        RouteSelectionFragment.newInstance(),
                        RouteSelectionFragment.TAG
                    )
                    .commit()

                viewModel.openRouteSelection.value = null
            }
        }
        viewModel.updatePersonnelStationResponse.observe(this) {
            if (it != null) {
                viewModel.updatePersonnelStationResponse.value = null
                viewModel.bottomSheetVisibility.value = false
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN
                FlexigoInfoDialog.Builder(this)
                    .setText1(getString(R.string.update_personnel_station_response_dialog_text))
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }

        viewModel.reservationAdded.observe(this) {
            if (it != null) {
                viewModel.bottomSheetVisibility.value = false
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN
                viewModel.searchedRoutes.value = null
                viewModel.searchedStops.value = null
                viewModel.reservationAdded.value = null

                if (viewModel.isVisibleMessage == true) {
                    FlexigoInfoDialog.Builder(this)
                        .setTitle(getString(R.string.shuttle_reservation_success_title))
                        .setText1(getString(R.string.shuttle_reservation_success_text))
                        .setCancelable(false)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                            viewModel.getMyNextRides()
                        }
                        .create()
                        .show()
                } else
                    viewModel.getMyNextRides()


                if (viewModel.isShuttleServicePlaningFragment) {
                    viewModel.navigator?.showInformationFragment()
                }
            }
        }

        viewModel.reservationCancelled.observe(this) {
            if (it != null) {
                viewModel.bottomSheetVisibility.value = false
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN
                viewModel.searchedRoutes.value = null
                viewModel.searchedStops.value = null
                viewModel.reservationCancelled.value = null

                FlexigoInfoDialog.Builder(this)
                    .setTitle(getString(R.string.shuttle_reservation_success_title))
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                        dialog.dismiss()
                        finish()
                        viewModel.getMyNextRides()
                    }
                    .create()
                    .show()

            }
        }

        viewModel.demandWorkgroupResponse.observe(this) {
            if (it != null) {
                viewModel.bottomSheetVisibility.value = false
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_HIDDEN
                viewModel.searchedRoutes.value = null
                viewModel.searchedStops.value = null
                viewModel.demandWorkgroupResponse.value = null

                if (it.error != null) {
                    it.error!!.message?.let { it1 ->
                        AppDialog.Builder(this)
                            .setIconVisibility(false)
                            .setSubtitle(it1)
                            .setOkButton(R.string.got_it_2) { d ->
                                d.dismiss()
                            }
                            .create().show()
                    }
                } else {
                    FlexigoInfoDialog.Builder(this)
                        .setTitle(getString(R.string.shuttle_workgroup_success_title))
                        .setText1(getString(R.string.shuttle_workgroup_success_text))
                        .setCancelable(false)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()

                            viewModel.getMyNextRides()

                        }
                        .create()
                        .show()
                }
            }
        }

        viewModel.bottomSheetBehaviorEditShuttleState.observe(this) {
            if (it != null) {

                bottomSheetBehaviorEditShuttle.state = it

                viewModel.bottomSheetBehaviorEditShuttleState.value = null
            }
        }

        viewModel.bottomSheetVisibility.observe(this) {
            if (it != null) {
                if (it) {
                    viewModel.navigator?.changeBottomNavigatorVisibility(false)
                    binding.viewBackgroundBottomSheet.visibility = View.VISIBLE
                } else {
                    viewModel.navigator?.changeBottomNavigatorVisibility(true)
                    binding.viewBackgroundBottomSheet.visibility = View.GONE
                }
            }
        }

        viewModel.noRoutesForEdit.observe(this) {
            if (it == true) {

                viewModel.isFromChanged = false
                viewModel.isToChanged = false
                viewModel.allNextRides.value?.let {
                    val myNextRides = viewModel.nextRides.value
                    viewModel.currentRide = myNextRides?.get(viewModel.currentMyRideIndex)

                    viewModel.currentRide?.let { currentRide ->

                        viewModel.clearSelections()
                        viewModel.bottomSheetVisibility.value = true

                        viewModel.currentDay.value =
                            currentRide.firstDepartureDate.getDateWithZeroHour()

                        setDatesForEditShuttle(
                            destinationId = currentRide.fromTerminalReferenceId
                                ?: currentRide.toTerminalReferenceId ?: 0,
                            fromType = currentRide.fromType,
                            isFirstOpen = true,
                            isFromSelectDestination = false
                        )

                        val myDestinationId = currentRide.toTerminalReferenceId
                            ?: currentRide.fromTerminalReferenceId

                        var myDestination: DestinationModel? = null
                        var myDestinationIndex: Int? = null

                        viewModel.destinations.value?.let { destinations ->
                            destinations.forEachIndexed { index, destinationModel ->
                                if (destinationModel.id == myDestinationId) {
                                    myDestination = destinationModel
                                    myDestinationIndex = index
                                }
                            }

                            if (myDestination == null) {
                                myDestination = destinations[0]
                                myDestinationIndex = 0
                            }
                        }

                        viewModel.textViewBottomSheetEditShuttleRouteName.value =
                            currentRide.firstDepartureDate.convertToShuttleReservationDate()
                                .plus(" ")
                                .plus(
                                    if (currentRide.fromTerminalReferenceId == null) getString(R.string.to_campus) else getString(
                                        R.string.from_campus
                                    )
                                )

                        if (currentRide.fromTerminalReferenceId == null) {
                            viewModel.selectedToDestination = myDestination
                            viewModel.selectedToDestinationIndex = myDestinationIndex
                        } else {
                            viewModel.selectedFromDestination = myDestination
                            viewModel.selectedFromDestinationIndex = myDestinationIndex
                        }

                        viewModel.searchRoutesAdapterSetMyDestinationTrigger.value = myDestination


                        val homeLocationModel = AppDataManager.instance.personnelInfo?.homeLocation

                        val homeLocation = Location("")
                        homeLocation.latitude = homeLocationModel?.latitude ?: 0.0
                        homeLocation.longitude = homeLocationModel?.longitude ?: 0.0

                        if (currentRide.fromTerminalReferenceId == null) {
                            viewModel.selectedFromLocation = ShuttleViewModel.FromToLocation(
                                location = homeLocation,
                                text = getString(R.string.home_location),
                                destinationId = null
                            )
                            viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
                                viewModel.selectedFromLocation?.text
                        } else {
                            viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                                location = homeLocation,
                                text = getString(R.string.home_location),
                                destinationId = null
                            )
                            viewModel.textViewBottomSheetEditShuttleRouteTo.value =
                                viewModel.selectedToLocation?.text
                        }


                        viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
                            if (currentRide.fromTerminalReferenceId == null) viewModel.selectedFromLocation?.text else viewModel.selectedFromDestination?.title
                        viewModel.textViewBottomSheetEditShuttleRouteTo.value =
                            if (currentRide.fromTerminalReferenceId == null) viewModel.selectedToDestination?.title else viewModel.selectedToLocation?.text


                        viewModel.switchBottomSheetEditShuttleUse.value = currentRide.notUsing

                        viewModel.openBottomSheetEditShuttle.value = true


                    }
                    if (viewModel.currentRide == null) {
                        viewModel.allNextRides.value?.let { allNextRides ->

                            viewModel.currentRide = allNextRides[0]

                            viewModel.clearSelections()
                            viewModel.bottomSheetVisibility.value = true

                            viewModel.currentDay.value =
                                viewModel.currentRide?.firstDepartureDate?.getDateWithZeroHour()
                                    ?: 0L

                            val nextDay = viewModel.currentDay.value!! + 1000L * 60 * 60 * 24

                            viewModel.selectedDate = null
                            viewModel.selectedDateIndex = null

                            val dateAndWorkgroupMap =
                                mutableMapOf<Long, ShuttleViewModel.DateAndWorkgroup>()

                            val destinationId = viewModel.currentRide?.fromTerminalReferenceId
                                ?: viewModel.currentRide?.toTerminalReferenceId ?: 0

                            allNextRides.forEach { nextRide ->
                                if ((viewModel.currentRide?.fromType == nextRide.fromType)
                                    && nextRide.firstDepartureDate in viewModel.currentDay.value!! until nextDay
                                ) {

                                    if (destinationId == nextRide.fromTerminalReferenceId || destinationId == nextRide.toTerminalReferenceId) {

                                        dateAndWorkgroupMap[nextRide.firstDepartureDate] =
                                            ShuttleViewModel.DateAndWorkgroup(
                                                nextRide.firstDepartureDate,
                                                nextRide.workgroupInstanceId,
                                                nextRide.workgroupStatus,
                                                nextRide.fromType,
                                                nextRide.fromTerminalReferenceId,
                                                nextRide,
                                                null
                                            )

                                    }
                                }
                            }


                            viewModel.dateAndWorkgroupList = dateAndWorkgroupMap.values.toList()

                            viewModel.dateAndWorkgroupList!!.forEachIndexed { index, dateWithWorkgroup ->
                                if (viewModel.currentRide?.firstDepartureDate == dateWithWorkgroup.date) {
                                    viewModel.selectedDate = dateWithWorkgroup
                                    viewModel.selectedDateIndex = index
                                }
                            }

                            if (viewModel.selectedDate == null) {
                                viewModel.selectedDate = viewModel.dateAndWorkgroupList!![0]
                                viewModel.selectedDateIndex = 0
                            }

                            viewModel.textViewBottomSheetEditShuttleRouteTime.value =
                                viewModel.selectedDate?.date.convertToShuttleDateTime()


                            /*********/

                            val myDestinationId = viewModel.currentRide?.toTerminalReferenceId
                                ?: viewModel.currentRide?.fromTerminalReferenceId

                            var myDestination: DestinationModel? = null
                            var myDestinationIndex: Int? = null

                            //fillDestinations()

                            viewModel.destinations.value?.let { destinations ->
                                destinations.forEachIndexed { index, destinationModel ->
                                    if (destinationModel.id == myDestinationId) {
                                        myDestination = destinationModel
                                        myDestinationIndex = index
                                    }
                                }

                                if (myDestination == null) {
                                    myDestination = destinations[0]
                                    myDestinationIndex = 0
                                }
                            }

                            viewModel.textViewBottomSheetEditShuttleRouteName.value =
                                viewModel.currentRide?.firstDepartureDate.convertToShuttleReservationDate()
                                    .plus(" ")
                                    .plus(
                                        if (viewModel.currentRide?.fromTerminalReferenceId == null) getString(
                                            R.string.to_campus
                                        ) else getString(R.string.from_campus)
                                    )

                            if (viewModel.currentRide?.fromTerminalReferenceId == null) {
                                viewModel.selectedToDestination = myDestination
                                viewModel.selectedToDestinationIndex = myDestinationIndex
                            } else {
                                viewModel.selectedFromDestination = myDestination
                                viewModel.selectedFromDestinationIndex = myDestinationIndex
                            }

                            viewModel.searchRoutesAdapterSetMyDestinationTrigger.value =
                                myDestination


                            val homeLocationModel =
                                AppDataManager.instance.personnelInfo?.homeLocation

                            val homeLocation = Location("")
                            homeLocation.latitude = homeLocationModel?.latitude ?: 0.0
                            homeLocation.longitude = homeLocationModel?.longitude ?: 0.0

                            if (viewModel.currentRide?.fromTerminalReferenceId == null) {
                                viewModel.selectedFromLocation = ShuttleViewModel.FromToLocation(
                                    location = homeLocation,
                                    text = getString(R.string.home_location),
                                    destinationId = null
                                )
                                viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
                                    viewModel.selectedFromLocation?.text
                            } else {
                                viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                                    location = homeLocation,
                                    text = getString(R.string.home_location),
                                    destinationId = null
                                )
                                viewModel.textViewBottomSheetEditShuttleRouteTo.value =
                                    viewModel.selectedToLocation?.text
                            }



                            viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
                                if (viewModel.currentRide?.fromTerminalReferenceId == null) viewModel.selectedFromLocation?.text else viewModel.selectedFromDestination?.title
                            viewModel.textViewBottomSheetEditShuttleRouteTo.value =
                                if (viewModel.currentRide?.fromTerminalReferenceId == null) viewModel.selectedToDestination?.title else viewModel.selectedToLocation?.text

                            viewModel.openBottomSheetEditShuttle.value = true

                        }

                    }
                }

            }
        }

        viewModel.editClicked.observe(this) {
            if (it == true) {
                viewModel.isFromChanged = false
                viewModel.isToChanged = false

                viewModel.allNextRides.value?.let {
                    val myNextRides = viewModel.nextRides.value
                    viewModel.currentRide = myNextRides?.get(viewModel.currentMyRideIndex)

                    viewModel.currentRide?.let { currentRide ->
                        if (currentRide.reserved) {

                            FlexigoInfoDialog.Builder(this)
                                .setTitle(getString(R.string.shuttle_demand_cancel))
                                .setText1(
                                    getString(
                                        R.string.shuttle_demand_cancel_info,
                                        currentRide.firstDepartureDate.convertToShuttleReservationTime2()
                                    )
                                )
                                .setCancelable(false)
                                .setIconVisibility(false)
                                .setOkButton(getString(R.string.Generic_Continue)) { dialog ->
                                    dialog.dismiss()
                                    val firstLeg = currentRide.firstLeg

                                    viewModel.cancelShuttleReservation2(
                                        request = ShuttleReservationRequest2(
                                            reservationDay = Date(currentRide.firstDepartureDate).convertForBackend2(),
                                            reservationDayEnd = null,
                                            workgroupInstanceId = currentRide.workgroupInstanceId,
                                            routeId = currentRide.routeId ?: 0,
                                            useFirstLeg = if (firstLeg) false else null,
                                            firstLegStationId = null,
                                            useReturnLeg = if (firstLeg.not()) false else null,
                                            returnLegStationId = null
                                        )
                                    )
                                }
                                .setCancelButton(getString(R.string.Generic_Close)) { dialog ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()


                        } else if (currentRide.routeId == null) {

                            FlexigoInfoDialog.Builder(this)
                                .setTitle(getString(R.string.shuttle_demand_cancel))
                                .setText1(
                                    getString(
                                        R.string.shuttle_demand_cancel_info,
                                        currentRide.firstDepartureDate.convertToShuttleReservationTime2()
                                    )
                                )
                                .setCancelable(false)
                                .setIconVisibility(false)
                                .setOkButton(getString(R.string.Generic_Continue)) { dialog ->
                                    dialog.dismiss()

                                    viewModel.cancelDemandWorkgroup(
                                        WorkgroupDemandRequest(
                                            workgroupInstanceId = currentRide.workgroupInstanceId,
                                            stationId = null,
                                            location = null
                                        )
                                    )

                                }
                                .setCancelButton(getString(R.string.Generic_Close)) { dialog ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                        } else {
                            viewModel.clearSelections()
                            viewModel.bottomSheetVisibility.value = true

                            viewModel.currentDay.value =
                                currentRide.firstDepartureDate.getDateWithZeroHour()

                            setDatesForEditShuttle(
                                destinationId = currentRide.fromTerminalReferenceId
                                    ?: currentRide.toTerminalReferenceId ?: 0,
                                fromType = currentRide.fromType,
                                isFirstOpen = true,
                                isFromSelectDestination = false
                            )

                            val myDestinationId = currentRide.toTerminalReferenceId
                                ?: currentRide.fromTerminalReferenceId

                            var myDestination: DestinationModel? = null
                            var myDestinationIndex: Int? = null

                            //fillDestinations()

                            viewModel.destinations.value?.let { destinations ->
                                destinations.forEachIndexed { index, destinationModel ->
                                    if (destinationModel.id == myDestinationId) {
                                        myDestination = destinationModel
                                        myDestinationIndex = index
                                    }
                                }

                                if (myDestination == null) {
                                    myDestination = destinations[0]
                                    myDestinationIndex = 0
                                }
                            }

                            viewModel.textViewBottomSheetEditShuttleRouteName.value =
                                currentRide.firstDepartureDate.convertToShuttleReservationDate()
                                    .plus(" ")
                                    .plus(
                                        if (currentRide.fromTerminalReferenceId == null) getString(
                                            R.string.to_campus
                                        ) else getString(R.string.from_campus)
                                    )

                            if (currentRide.fromTerminalReferenceId == null) {
                                viewModel.selectedToDestination = myDestination
                                viewModel.selectedToDestinationIndex = myDestinationIndex
                            } else {
                                viewModel.selectedFromDestination = myDestination
                                viewModel.selectedFromDestinationIndex = myDestinationIndex
                            }

                            viewModel.searchRoutesAdapterSetMyDestinationTrigger.value =
                                myDestination


                            val homeLocationModel =
                                AppDataManager.instance.personnelInfo?.homeLocation

                            val homeLocation = Location("")
                            homeLocation.latitude = homeLocationModel?.latitude ?: 0.0
                            homeLocation.longitude = homeLocationModel?.longitude ?: 0.0

                            if (currentRide.fromTerminalReferenceId == null) {
                                viewModel.selectedFromLocation = ShuttleViewModel.FromToLocation(
                                    location = homeLocation,
                                    text = getString(R.string.home_location),
                                    destinationId = null
                                )
                                viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
                                    viewModel.selectedFromLocation?.text
                            } else {
                                viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                                    location = homeLocation,
                                    text = getString(R.string.home_location),
                                    destinationId = null
                                )
                                viewModel.textViewBottomSheetEditShuttleRouteTo.value =
                                    viewModel.selectedToLocation?.text
                            }

                            viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
                                if (currentRide.fromTerminalReferenceId == null) viewModel.selectedFromLocation?.text else viewModel.selectedFromDestination?.title
                            viewModel.textViewBottomSheetEditShuttleRouteTo.value =
                                if (currentRide.fromTerminalReferenceId == null) viewModel.selectedToDestination?.title else viewModel.selectedToLocation?.text


                            viewModel.switchBottomSheetEditShuttleUse.value = currentRide.notUsing

                            viewModel.openBottomSheetEditShuttle.value = true

                        }
                    }
                    if (viewModel.currentRide == null) {
                        viewModel.allNextRides.value?.let { allNextRides ->

                            viewModel.currentRide = allNextRides[0]

                            viewModel.clearSelections()
                            viewModel.bottomSheetVisibility.value = true

                            viewModel.currentDay.value =
                                viewModel.currentRide?.firstDepartureDate?.getDateWithZeroHour()
                                    ?: 0L

                            val nextDay = viewModel.currentDay.value!! + 1000L * 60 * 60 * 24

                            viewModel.selectedDate = null
                            viewModel.selectedDateIndex = null

                            val dateAndWorkgroupMap =
                                mutableMapOf<Long, ShuttleViewModel.DateAndWorkgroup>()

                            val destinationId = viewModel.currentRide?.fromTerminalReferenceId
                                ?: viewModel.currentRide?.toTerminalReferenceId ?: 0

                            allNextRides.forEach { nextRide ->
                                if ((viewModel.currentRide?.fromType == nextRide.fromType)
                                    && nextRide.firstDepartureDate in viewModel.currentDay.value!! until nextDay
                                ) {

                                    if (destinationId == nextRide.fromTerminalReferenceId
                                        || destinationId == nextRide.toTerminalReferenceId
                                    ) {

                                        dateAndWorkgroupMap[nextRide.firstDepartureDate] =
                                            ShuttleViewModel.DateAndWorkgroup(
                                                nextRide.firstDepartureDate,
                                                nextRide.workgroupInstanceId,
                                                nextRide.workgroupStatus,
                                                nextRide.fromType,
                                                nextRide.fromTerminalReferenceId,
                                                nextRide,
                                                null
                                            )

                                    }
                                }
                            }


                            viewModel.dateAndWorkgroupList = dateAndWorkgroupMap.values.toList()

                            viewModel.dateAndWorkgroupList!!.forEachIndexed { index, dateWithWorkgroup ->
                                if (viewModel.currentRide?.firstDepartureDate == dateWithWorkgroup.date) {
                                    viewModel.selectedDate = dateWithWorkgroup
                                    viewModel.selectedDateIndex = index
                                }
                            }

                            if (viewModel.selectedDate == null) {
                                viewModel.selectedDate = viewModel.dateAndWorkgroupList!![0]
                                viewModel.selectedDateIndex = 0
                            }

                            viewModel.textViewBottomSheetEditShuttleRouteTime.value =
                                viewModel.selectedDate?.date.convertToShuttleDateTime()


                            val myDestinationId = viewModel.currentRide?.toTerminalReferenceId
                                ?: viewModel.currentRide?.fromTerminalReferenceId

                            var myDestination: DestinationModel? = null
                            var myDestinationIndex: Int? = null

                            viewModel.destinations.value?.let { destinations ->
                                destinations.forEachIndexed { index, destinationModel ->
                                    if (destinationModel.id == myDestinationId) {
                                        myDestination = destinationModel
                                        myDestinationIndex = index
                                    }
                                }

                                if (myDestination == null) {
                                    myDestination = destinations[0]
                                    myDestinationIndex = 0
                                }
                            }

                            viewModel.textViewBottomSheetEditShuttleRouteName.value =
                                viewModel.currentRide?.firstDepartureDate.convertToShuttleReservationDate()
                                    .plus(" ")
                                    .plus(
                                        if (viewModel.currentRide?.fromTerminalReferenceId == null) getString(
                                            R.string.to_campus
                                        ) else getString(R.string.from_campus)
                                    )

                            if (viewModel.currentRide?.fromTerminalReferenceId == null) {
                                viewModel.selectedToDestination = myDestination
                                viewModel.selectedToDestinationIndex = myDestinationIndex
                            } else {
                                viewModel.selectedFromDestination = myDestination
                                viewModel.selectedFromDestinationIndex = myDestinationIndex
                            }

                            viewModel.searchRoutesAdapterSetMyDestinationTrigger.value =
                                myDestination


                            val homeLocationModel =
                                AppDataManager.instance.personnelInfo?.homeLocation

                            val homeLocation = Location("")
                            homeLocation.latitude = homeLocationModel?.latitude ?: 0.0
                            homeLocation.longitude = homeLocationModel?.longitude ?: 0.0

                            if (viewModel.currentRide?.fromTerminalReferenceId == null) {
                                viewModel.selectedFromLocation = ShuttleViewModel.FromToLocation(
                                    location = homeLocation,
                                    text = getString(R.string.home_location),
                                    destinationId = null
                                )
                                viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
                                    viewModel.selectedFromLocation?.text
                            } else {
                                viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                                    location = homeLocation,
                                    text = getString(R.string.home_location),
                                    destinationId = null
                                )
                                viewModel.textViewBottomSheetEditShuttleRouteTo.value =
                                    viewModel.selectedToLocation?.text
                            }



                            viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
                                if (viewModel.currentRide?.fromTerminalReferenceId == null) viewModel.selectedFromLocation?.text else viewModel.selectedFromDestination?.title
                            viewModel.textViewBottomSheetEditShuttleRouteTo.value =
                                if (viewModel.currentRide?.fromTerminalReferenceId == null) viewModel.selectedToDestination?.title else viewModel.selectedToLocation?.text

                            viewModel.openBottomSheetEditShuttle.value = true

                        }

                    }
                }
            }
        }

        updateSessionCount()
        showAppRating()
    }

    private fun updateSessionCount() {
        val isUpdateSessionCount = AppDataManager.instance.isUpdateSessionCount

        if (isUpdateSessionCount == true) {
            var sessionCount = AppDataManager.instance.sessionCount
            sessionCount += 1
            AppDataManager.instance.sessionCount = sessionCount
            AppDataManager.instance.isUpdateSessionCount = false

        }

    }

    private fun isChangeMajorVersion(major: Int): Boolean {
        //major version geldiğinde bilgiler sıfırlanır.
        if (!AppDataManager.instance.lastVersion.equals("") && AppDataManager.instance.lastVersion!!.split(".").first().toInt() < major) {
            AppDataManager.instance.showReview = false
            AppDataManager.instance.sessionCount = 0
            return true
        }

        return false
    }

    private var major = 0

    private fun showAppRating() {
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            major = versionName.split(".").first().toInt()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (isChangeMajorVersion(major) ||
            (!AppDataManager.instance.sameSession!! && !AppDataManager.instance.showReview && AppDataManager.instance.sessionCount != 0
                    && ((AppDataManager.instance.sessionCount == 3 || AppDataManager.instance.sessionCount == AppDataManager.instance.tempCount)
                    && AppDataManager.instance.sessionCount <= 48))
        ) {
            AppDataManager.instance.tempCount = AppDataManager.instance.sessionCount * 2
            AppDataManager.instance.sameSession = true

            val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            AppDataManager.instance.lastVersion = pInfo.versionName

            val dialog = AlertDialog.Builder(this)
            dialog.setCancelable(false)
            dialog.setTitle(fromHtml("<b>${resources.getString(R.string.feedback)}</b>"))
            dialog.setMessage(resources.getString(R.string.impression_title))
            dialog.setPositiveButton(resources.getString(R.string.love_it)) { d, _ ->
                d.dismiss()
                showRateApp()
            }
            dialog.setNeutralButton(resources.getString(R.string.could_be_better)) { d, _ ->

                val intent = Intent(this, CommentsActivity::class.java)
                startActivity(intent)

                d.dismiss()
            }

            dialog.show()
        }
    }

    private fun showRateApp() {
        val reviewManager = ReviewManagerFactory.create(this)

        val request: Task<ReviewInfo> = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Getting the ReviewInfo object
                val reviewInfo: ReviewInfo = task.result
                val flow: Task<Void> = reviewManager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { task1 ->
                    AppDataManager.instance.showReview = true
                    showThanksDialog()
                }
            } else
                Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showThanksDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setCancelable(true)
        dialog.setTitle(fromHtml("<b>${resources.getString(R.string.send_comment)}</b>"))
        dialog.setMessage(resources.getString(R.string.thanks_for_improve))
        dialog.setNeutralButton(resources.getString(R.string.mdtp_ok)) { d, _ ->
            d.dismiss()
        }

        dialog.show()
    }

    private fun setBottomSheetData() {

        viewModel.clearSelections()
        viewModel.bottomSheetVisibility.value = true

        viewModel.selectedDate = null
        viewModel.selectedDateIndex = null
        viewModel.dateAndWorkgroupList = null

        val myDestinationId = viewModel.workgroupTemplate?.toTerminalReferenceId
            ?: viewModel.workgroupTemplate?.fromTerminalReferenceId

        val dateAndWorkgroupMap = mutableMapOf<Long?, ShuttleViewModel.DateAndWorkgroup>()


        viewModel.workgroupTemplateList.value?.forEach { template ->
            if (viewModel.workgroupInstance?.name?.contains(template.name!!) == true) {
                viewModel.workGroupSameNameList.value?.find {
                    it.templateId == template.id
                }?.let {
                    if (viewModel.workgroupTemplate?.fromType == template.fromType) {
                        if (myDestinationId == template.fromTerminalReferenceId || myDestinationId == template.toTerminalReferenceId) {

                            dateAndWorkgroupMap[it.firstDepartureDate ?: 0] =
                                ShuttleViewModel.DateAndWorkgroup(
                                    it.firstDepartureDate!!,
                                    it.id,
                                    it.workgroupStatus,
                                    template.fromType!!,
                                    template.fromTerminalReferenceId,
                                    ShuttleNextRide(
                                        template.id,
                                        it.id,
                                        it.firstDepartureDate!!,
                                        0L,
                                        it.workgroupStatus,
                                        template.name!!,
                                        0L,
                                        "",
                                        0L,
                                        "",
                                        template.workgroupType,
                                        0L,
                                        template.fromType,
                                        template.fromTerminalReferenceId,
                                        template.toType!!,
                                        template.toTerminalReferenceId,
                                        template.direction,
                                        false,
                                        false,
                                        false,
                                        false
                                    ),
                                    template
                                )

                        }
                    }
                } ?: run {
                    if (template.id == viewModel.workgroupInstance?.templateId) {
                        if (viewModel.workgroupTemplate?.fromType == template.fromType) {
                            if (myDestinationId == template.fromTerminalReferenceId || myDestinationId == template.toTerminalReferenceId) {
                                dateAndWorkgroupMap[viewModel.workgroupInstance?.firstDepartureDate
                                    ?: 0] = ShuttleViewModel.DateAndWorkgroup(
                                    viewModel.workgroupInstance?.firstDepartureDate ?: 0,
                                    viewModel.workgroupInstance!!.id,
                                    viewModel.workgroupInstance!!.workgroupStatus,
                                    template.fromType!!,
                                    template.fromTerminalReferenceId,
                                    ShuttleNextRide(
                                        template.id,
                                        viewModel.workgroupInstance!!.id,
                                        viewModel.workgroupInstance!!.firstDepartureDate ?: 0,
                                        0L,
                                        viewModel.workgroupInstance!!.workgroupStatus,
                                        template.name!!,
                                        0L,
                                        "",
                                        0L,
                                        "",
                                        template.workgroupType,
                                        0L,
                                        template.fromType,
                                        template.fromTerminalReferenceId,
                                        template.toType!!,
                                        template.toTerminalReferenceId,
                                        template.direction,
                                        false,
                                        false,
                                        false,
                                        false
                                    ),
                                    template
                                )

                            }
                        }
                    }
                }
            }
        }
        viewModel.dateAndWorkgroupList = dateAndWorkgroupMap.values.toList()


        viewModel.dateAndWorkgroupList!!.forEachIndexed { index, dateWithWorkgroup ->
            if (viewModel.selectedDate?.date == dateWithWorkgroup.date) {
                viewModel.selectedDate = dateWithWorkgroup
                viewModel.selectedDateIndex = index
            }
        }

        if (viewModel.selectedDate == null) {
            viewModel.selectedDate = viewModel.dateAndWorkgroupList!![0]
            viewModel.selectedDateIndex = 0
        }

        viewModel.workgroupTemplate?.let {
            if (it.direction == WorkgroupDirection.ROUND_TRIP) {
                viewModel.textViewBottomSheetEditShuttleRouteTime.value =
                    ((it.shift?.departureHour ?: it.shift?.arrivalHour).convertHourMinutes()
                        ?: "") + "-" + ((it.shift?.returnDepartureHour
                        ?: it.shift?.returnArrivalHour).convertHourMinutes() ?: "")
            } else
                viewModel.textViewBottomSheetEditShuttleRouteTime.value =
                    viewModel.selectedDate?.date.convertToShuttleDateTime()
        }

        viewModel.isShuttleTimeMultiple = viewModel.dateAndWorkgroupList!!.size > 1

        var myDestination: DestinationModel? = null
        var myDestinationIndex: Int? = null

        viewModel.destinations.value?.let { destinations ->
            destinations.forEachIndexed { index, destinationModel ->
                if (destinationModel.id == myDestinationId) {
                    myDestination = destinationModel
                    myDestinationIndex = index
                }
            }

            if (myDestination == null) {
                myDestination = destinations[0]
                myDestinationIndex = 0
            }
        }

        if (viewModel.workgroupTemplate?.fromTerminalReferenceId == null) {
            viewModel.selectedToDestination = myDestination
            viewModel.selectedToDestinationIndex = myDestinationIndex
        } else {
            viewModel.selectedFromDestination = myDestination
            viewModel.selectedFromDestinationIndex = myDestinationIndex
        }


        viewModel.textViewBottomSheetEditShuttleRouteName.value = viewModel.workgroupTemplate?.name

        val homeLocationModel = AppDataManager.instance.personnelInfo?.homeLocation

        val homeLocation = Location("")
        homeLocation.latitude = homeLocationModel?.latitude ?: 0.0
        homeLocation.longitude = homeLocationModel?.longitude ?: 0.0

        if (viewModel.workgroupTemplate?.fromTerminalReferenceId == null) {
            viewModel.selectedFromLocation = ShuttleViewModel.FromToLocation(
                location = homeLocation,
                text = getString(R.string.home_location),
                destinationId = null
            )
            viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
                viewModel.selectedFromLocation?.text
        } else {
            viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                location = homeLocation,
                text = getString(R.string.home_location),
                destinationId = null
            )
            viewModel.textViewBottomSheetEditShuttleRouteTo.value =
                viewModel.selectedToLocation?.text

        }

        viewModel.textViewBottomSheetEditShuttleRouteFrom.value =
            if (viewModel.workgroupTemplate?.fromTerminalReferenceId == null) viewModel.selectedFromLocation?.text else viewModel.selectedFromDestination?.title
        viewModel.textViewBottomSheetEditShuttleRouteTo.value =
            if (viewModel.workgroupTemplate?.fromTerminalReferenceId == null) viewModel.selectedToDestination?.title else viewModel.selectedToLocation?.text

    }

    private fun setDatesForEditShuttle(
        destinationId: Long,
        fromType: FromToType,
        isFirstOpen: Boolean,
        isFromSelectDestination: Boolean
    ) {

        viewModel.allNextRides.value?.let { allNextRides ->

            val nextDay = viewModel.currentDay.value!! + 1000L * 60 * 60 * 24

            viewModel.selectedDate = null
            viewModel.selectedDateIndex = null
            viewModel.dateAndWorkgroupList = null

            val dateAndWorkgroupMap = mutableMapOf<Long, ShuttleViewModel.DateAndWorkgroup>()

            var i = 0L
            allNextRides.forEach { nextRide ->
                if ((fromType == nextRide.fromType)
                    && nextRide.firstDepartureDate in viewModel.currentDay.value!! until nextDay
                ) {

                    if (destinationId == nextRide.fromTerminalReferenceId
                        || destinationId == nextRide.toTerminalReferenceId
                    ) {

//                        dateAndWorkgroupMap[nextRide.firstDepartureDate] =
                        dateAndWorkgroupMap[i] =
                            ShuttleViewModel.DateAndWorkgroup(
                                nextRide.firstDepartureDate,
                                nextRide.workgroupInstanceId,
                                nextRide.workgroupStatus,
                                nextRide.fromType,
                                nextRide.fromTerminalReferenceId,
                                nextRide,
                                null
                            )
                        i++
                    }
                }
            }

            viewModel.dateAndWorkgroupList = dateAndWorkgroupMap.values.toList()

            if (isFirstOpen) {

                viewModel.dateAndWorkgroupList!!.forEachIndexed { index, dateWithWorkgroup ->
                    if (viewModel.currentRide?.firstDepartureDate == dateWithWorkgroup.date && viewModel.currentRide?.workgroupInstanceId == dateWithWorkgroup.workgroupId) {
                        viewModel.selectedDate = dateWithWorkgroup
                        viewModel.selectedDateIndex = index
                    }
                }
                val list = viewModel.dateAndWorkgroupList
                if (viewModel.selectedDate == null && list != null && list.isNotEmpty()) {
                    viewModel.selectedDate = list[0]
                    viewModel.selectedDateIndex = 0
                }

            } else {
                viewModel.dateAndWorkgroupList!!.forEachIndexed { index, dateWithWorkgroup ->

                    if (viewModel.selectedDate?.date == dateWithWorkgroup.date) {
                        viewModel.selectedDate = dateWithWorkgroup
                        viewModel.selectedDateIndex = index
                    }
                }
                val list = viewModel.dateAndWorkgroupList
                if (viewModel.selectedDate == null && list != null && list.isNotEmpty()) {
                    viewModel.selectedDate = list[0]
                    viewModel.selectedDateIndex = 0
                }
            }
            viewModel.textViewBottomSheetEditShuttleRouteTime.value =
                viewModel.selectedDate?.date.convertToShuttleDateTime()

        }
    }

    override fun onPause() {
        super.onPause()
        locationClient?.stop()
    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = ViewModelProvider(this, factory)[ShuttleViewModel::class.java]
        return viewModel
    }

    private fun continueAfterPersonnelInfo(savedInstanceState: Bundle?) {
        val routeId = AppDataManager.instance.personnelInfo?.routeId
        if (routeId != null) {
            if (routeId != 0L) {
                viewModel.getRouteDetailsById(routeId)
            }
            if (savedInstanceState == null) {
                supportFragmentManager
                    .beginTransaction()
                    .add(
                        R.id.fragment_container_view,
                        ShuttleMainFragment.newInstance(),
                        ShuttleMainFragment.TAG
                    )
                    .commit()
            }

            binding.bottomNavigation.setOnNavigationItemSelectedListener {
                val currentFragment: Fragment? = getCurrentFragment()

                when (it.itemId) {
                    R.id.menu_shuttle_shuttle -> {
                        if ((currentFragment is ShuttleMainFragment).not()) {
                            if (currentFragment is ShuttleQrReaderFragment) {
                                supportFragmentManager.popBackStack()
                            }
                            showShuttleMainFragment()
                        }
                    }
                    R.id.menu_shuttle_information -> {
                        viewModel.fromPlace.value = null
                        viewModel.toPlace.value = null
                        viewModel.shifts.value = null
                        setRouteFilterVisibility(false)
                        if ((currentFragment is ShuttleInformationFragment).not()) {
                            if ((currentFragment is ShuttleMainFragment).not()) {
                                supportFragmentManager.popBackStack()
                                if (currentFragment is ShuttleQrReaderFragment) {
                                    supportFragmentManager.popBackStack()
                                }
                            }
                            showInformationFragment()
                        }
                    }
                    R.id.menu_shuttle_qr_code -> {
                        viewModel.fromPlace.value = null
                        viewModel.toPlace.value = null
                        viewModel.shifts.value = null
                        setRouteFilterVisibility(false)
                        if ((currentFragment is ShuttleQrCodeFragment).not() && (currentFragment is ShuttleQrReaderFragment).not()) {
                            if ((currentFragment is ShuttleMainFragment).not()) {
                                supportFragmentManager.popBackStack()
                                if (currentFragment is ShuttleQrReaderFragment) {
                                    supportFragmentManager.popBackStack()
                                }
                            }
                            showQrCodeFragment()
                            if (AppDataManager.instance.isQrAutoOpen) {
                                scanQrCode(null)
                            }
                        }
                    }
                }
                return@setOnNavigationItemSelectedListener true
            }
        }
    }

    private fun getCurrentFragment(): Fragment? {
        var currentFragment: Fragment? = null
        if (supportFragmentManager.fragments.size > 0) {
            currentFragment = supportFragmentManager.fragments.last()
        }
        return currentFragment
    }

    override fun showShuttleMainFragment() {
        binding.textViewToolbarTitle.text = viewModel.myRouteDetails.value?.name
        viewModel.fromPlace.value = null
        viewModel.toPlace.value = null
        viewModel.shifts.value = null
        supportFragmentManager.popBackStack()
    }

    override fun showInformationFragment() {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_container_view,
                ShuttleServicePlanningFragment.newInstance(),
                ShuttleServicePlanningFragment.TAG
            )
            .addToBackStack(null)
            .commit()
    }

    override fun showServicePlanningReservationFragment() {

        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.fragment_container_view,
                ShuttleServicePlanningReservationFragment.newInstance(),
                ShuttleServicePlanningReservationFragment.TAG
            )
            .addToBackStack(null)
            .commit()
    }

    override fun showQrCodeFragment() {
        binding.textViewToolbarTitle.text = getString(R.string.shuttle_menu_qr_code)
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.fragment_container_view,
                ShuttleQrCodeFragment.newInstance(),
                ShuttleQrCodeFragment.TAG
            )
            .addToBackStack(null)
            .commit()
    }

    override fun setToolBarText(text: String) {
        binding.textViewToolbarTitle.text = text
    }

    override fun backPressed(view: View?) {
        onBackPressed()
    }

    override fun scanQrCode(view: View?) {

        if (checkAndRequestCameraPermission(this)) {
            onCameraPermissionOk()
        }

    }

    override fun onCameraPermissionOk() {
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.fragment_container_view,
                ShuttleQrReaderFragment.newInstance(),
                ShuttleQrReaderFragment.TAG
            )
            .addToBackStack(null)
            .commit()
    }

    override fun onCameraPermissionFailed() {

    }

    override fun onQrCodeCheckChanged(checked: Boolean) {
        AppDataManager.instance.isQrAutoOpen = checked
    }

    override fun qrReaderClose(view: View?) {
        supportFragmentManager.popBackStack()
    }

    override fun showRouteSearchFromFragment(view: View?) {

        if (viewModel.toPlace.value != null && viewModel.fromPlace.value != null) {
            viewModel.toPlace.value = null
            viewModel.fromPlace.value = null
            viewModel.shifts.value = null
        }

        binding.textViewToolbarTitle.text = getString(R.string.shuttle_route)
        viewModel.currentSearchType = ShuttleViewModel.SearchType.from
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.fragment_container_view,
                ShuttleRouteSearchFromToFragment.newInstance(),
                ShuttleRouteSearchFromToFragment.TAG
            )
            .addToBackStack(null)
            .commit()
    }

    override fun showRouteSearchToFragment(view: View?) {

        if (viewModel.toPlace.value != null && viewModel.fromPlace.value != null) {
            viewModel.toPlace.value = null
            viewModel.fromPlace.value = null
            viewModel.shifts.value = null
        }

        viewModel.currentSearchType = ShuttleViewModel.SearchType.to
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.fragment_container_view,
                ShuttleRouteSearchFromToFragment.newInstance(),
                ShuttleRouteSearchFromToFragment.TAG
            )
            .addToBackStack(null)
            .commit()
    }

    override fun showFromToMapFragment(view: View?) {

        if (getCurrentFragment() is ShuttleRouteSearchFromToFragment) {
            supportFragmentManager.popBackStack()
        }

        if (supportFragmentManager.findFragmentByTag(ShuttleFromToMapFragment.TAG) == null) {
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.fragment_container_view,
                    ShuttleFromToMapFragment.newInstance(),
                    ShuttleFromToMapFragment.TAG
                )
                .addToBackStack(null)
                .commit()
        }

    }

    override fun showMenuActivity(view: View?) {
        startActivity(Intent(this, MenuActivity::class.java))
    }

    override fun changeTitle(title: String) {
        binding.textViewToolbarTitle.text = title
    }

    override fun highlightSearchRouteFinished() {

        HighlightView.Builder(
            this@ShuttleActivity,
            binding.bottomNavigation.findViewById(R.id.menu_shuttle_information),
            this@ShuttleActivity,
            "shuttle_info",
            "sequence_shuttle_main"
        )
            .setHighlightText(getString(R.string.tutorial_shuttle_menu_info))
            .addGotItListener {
                HighlightView.Builder(
                    this@ShuttleActivity,
                    binding.bottomNavigation.findViewById(R.id.menu_shuttle_qr_code),
                    this@ShuttleActivity,
                    "shuttle_qr",
                    "sequence_shuttle_main"
                )
                    .setHighlightText(getString(R.string.tutorial_shuttle_menu_qr))
                    .addGotItListener {

                    }
                    .create()
            }
            .create()


    }

    override fun setRouteFilterVisibility(visibility: Boolean) {
        binding.buttonRouteFilter.visibility = if (visibility) View.VISIBLE else View.INVISIBLE
    }

    override fun showRouteFilter(view: View?) {
        viewModel.getShifts()
    }

    override fun changeBottomNavigatorVisibility(isVisible: Boolean) {
        binding.bottomNavigation.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun changeToolBarVisibility(isVisible: Boolean) {
        binding.layoutToolbar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {

        setRouteFilterVisibility(false)
        viewModel.fromPlace.value = null
        viewModel.toPlace.value = null
        viewModel.shifts.value = null

        if (viewModel.myRouteDetails.value != viewModel.routeDetails.value) {
            viewModel.routeDetails.value = viewModel.myRouteDetails.value
            binding.textViewToolbarTitle.text = getString(R.string.shuttle_route)
        } else {
            val currentFragment = getCurrentFragment()

            if (currentFragment is ShuttleQrReaderFragment
                || currentFragment is ShuttleRouteSearchFromToFragment
                || currentFragment is ShuttleFromToMapFragment
            ) {

                if (currentFragment is ShuttleFromToMapFragment) {
                    binding.textViewToolbarTitle.text = viewModel.myRouteDetails.value?.name
                } else if (currentFragment is ShuttleRouteSearchFromToFragment) {
                    binding.textViewToolbarTitle.text = viewModel.myRouteDetails.value?.name
                }

                super.onBackPressed()
            } else if (currentFragment is VanpoolDriverStationsFragment || currentFragment is VanpoolPassengerFragment) {
                supportFragmentManager.popBackStack()
            } else {
                showHomeActivity()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
        for (fragment in supportFragmentManager.fragments) {
            if (fragment is PermissionsUtils.StateListener) {
                PermissionsUtils.onRequestPermissionsResult(
                    requestCode,
                    grantResults,
                    fragment as PermissionsUtils.StateListener
                )
            }
        }
    }

    override fun onLocationPermissionOk() {
        if (locationClient == null) {
            locationClient = FusedLocationClient(this)
        }

        locationClient?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient?.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            override fun onLocationUpdated(location: Location) {

                viewModel.myLocation = location
                AppDataManager.instance.currentLocation = location
            }

            override fun onLocationFailed(message: String) {

                if (this@ShuttleActivity.isFinishing || this@ShuttleActivity.isDestroyed) {
                    return
                }

                when (message) {
                    FusedLocationClient.ERROR_LOCATION_DISABLED -> locationClient?.showLocationSettingsDialog()
                    FusedLocationClient.ERROR_LOCATION_MODE -> {
                        locationClient?.showLocationSettingsDialog()
                    }
                    FusedLocationClient.ERROR_TIMEOUT_OCCURRED -> {
                        handleError(RuntimeException(getString(R.string.location_timeout)))
                    }
                }
            }

        })
    }

    override fun onLocationPermissionFailed() {
    }


}
