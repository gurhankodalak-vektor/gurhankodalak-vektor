package com.vektortelekom.android.vservice.ui.shuttle.fragment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.ShuttleServicePlanningReservationFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.utils.*
import javax.inject.Inject

class ShuttleServicePlanningReservationFragment : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: ShuttleServicePlanningReservationFragmentBinding

    private var googleMap: GoogleMap? = null

    private var destinationLatLng: LatLng? = null

    private var selectedStation: StationModel? = null

    private var stationIcon: BitmapDescriptor? = null
    private var myStationIcon: BitmapDescriptor? = null
    private var homeIcon: BitmapDescriptor? = null
    private var workplaceIcon: BitmapDescriptor? = null

    private var isFirstStart = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<ShuttleServicePlanningReservationFragmentBinding>(inflater, R.layout.shuttle_service_planning_reservation_fragment, container, false).apply {
            lifecycleOwner = this@ShuttleServicePlanningReservationFragment
            viewModel = this@ShuttleServicePlanningReservationFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedStation = viewModel.selectedStation

        binding.mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync { map ->
            googleMap = map

            stationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_station)
            myStationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_my_station)
            workplaceIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_workplace)
            homeIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_home)

            updateSelectedStop()
            viewModel.selectedRoute?.let { routeModel ->
                viewModel.currentRoute = routeModel
                fillUI(routeModel)
            }

            map.setOnMarkerClickListener { marker ->
                markerClicked(marker)
            }
        }

        binding.textViewBottomSheetStopName.text = selectedStation?.title

        binding.textViewBottomSheetReservationDate.text = viewModel.selectedDate?.date.convertToShuttleReservationTime()

        binding.imageViewStopLocation.setOnClickListener {
            viewModel.navigateToMapTrigger.value = true
        }

        viewModel.navigateToMapTrigger.observe(viewLifecycleOwner) {
            if(it != null) {
                viewModel.selectedStation?.let { station ->
                    navigateToMap(viewModel.myLocation?.latitude?:0.0, viewModel.myLocation?.longitude?:0.0, station.location.latitude, station.location.longitude)
                }

                viewModel.navigateToMapTrigger.value = null
            }
        }

        binding.buttonBottomSheetReservationReserve.setOnClickListener {
            if (viewModel.selectedRoute?.personnelCount!! < viewModel.selectedRoute?.vehicleCapacity!!){

                viewModel.calendarSelectedDay.let { selectedDate ->
                    selectedStation?.let {

                        FlexigoInfoDialog.Builder(requireContext())
                            .setTitle(getString(R.string.reservation))
                            .setText1(getString(R.string.shuttle_make_reservation_info_text,
                                (viewModel.currentRoute?.title?:""),
                                selectedDate.convertForShuttleDay(), viewModel.selectedDate?.date.convertToShuttleDateTime()))
                            .setCancelable(false)
                            .setIconVisibility(false)
                            .setOkButton(getString(R.string.confirm)) { dialog ->
                                dialog.dismiss()
                                viewModel.selectedStation?.let { stop ->
                                    val requestModel =
                                        viewModel.getReservationRequestModel(stop)
                                    requestModel?.let { model ->
                                        viewModel.makeShuttleReservation2(model, isVisibleMessage = true)
                                    }

                                }
                            }
                            .setCancelButton(getString(R.string.cancel_2)) { dialog ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                    }
                }
            } else{

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.no_availability)
                builder.setMessage(R.string.full_route)
                    .setPositiveButton(R.string.Generic_Ok) { dialog, _ ->
                        dialog.dismiss()
                    }

                builder.create().show()
            }

        }

        binding.buttonBottomSheetReservationUsual.setOnClickListener {

            if (viewModel.selectedRoute?.personnelCount!! < viewModel.selectedRoute?.vehicleCapacity!!){

                FlexigoInfoDialog.Builder(requireContext())
                    .setTitle(getString(R.string.shuttle_change_info_title))
                    .setText1(getString(R.string.shuttle_change_info_text, viewModel.currentRoute?.title?:""))
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.generic_change)) { dialog ->
                        dialog.dismiss()
                        selectedStation?.let {
                            viewModel.updatePersonnelStation(
                                id = it.id
                            )
                        }
                    }
                    .setCancelButton(getString(R.string.cancel_2)) { dialog ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            } else{

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.no_availability)
                builder.setMessage(R.string.full_route)
                    .setPositiveButton(R.string.Generic_Ok) { dialog, _ ->
                        dialog.dismiss()
                    }

                builder.create().show()
            }

        }

        binding.buttonCallDriver.setOnClickListener {
            viewModel.selectedRoute?.let { it ->
                val phoneNumber: String = it.driver.phoneNumber

                AppDialog.Builder(requireContext())
                        .setCloseButtonVisibility(false)
                        .setIconVisibility(false)
                        .setTitle(getString(R.string.call_2))
                        .setSubtitle(getString(R.string.will_call, phoneNumber))
                        .setOkButton(getString(R.string.Generic_Ok)) { d ->
                            d.dismiss()

                            phoneNumber.let {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(it)))
                                startActivity(intent)
                            }

                        }
                        .setCancelButton(getString(R.string.cancel)) { d ->
                            d.dismiss()
                        }
                        .create().show()
            }
        }


        viewModel.reservationAdded.observe(viewLifecycleOwner) {
            if(it != null) {
                isFirstStart = false
                viewModel.reservationAdded.value = null

                FlexigoInfoDialog.Builder(requireContext())
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
            }
        }

        viewModel.myNextRides.observe(viewLifecycleOwner) {
            if(it != null && isFirstStart.not()) {
                isFirstStart = false
                viewModel.navigator?.showInformationFragment()
            }
        }

        viewModel.updatePersonnelStationResponse.observe(viewLifecycleOwner) {
            if(it != null) {
                viewModel.updatePersonnelStationResponse.value = null
                FlexigoInfoDialog.Builder(requireContext())
                        .setText1(getString(R.string.update_personnel_station_response_dialog_text))
                        .setCancelable(false)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                            viewModel.navigator?.showInformationFragment()
                        }
                        .create()
                        .show()

            }
        }

        binding.imageViewBottomSheetRoutesBack.setOnClickListener {
            viewModel.isReturningShuttleEdit = true
            childFragmentManager.popBackStack()
            if (!viewModel.fromPage.equals("") && viewModel.fromPage.equals("BottomSheetSearchRoute")) {
                viewModel.openBottomSheetSearchRoute.value = true
                viewModel.fromPage = ""
            }
            else
                viewModel.openBottomSheetRoutes.value = true

        }

    }
    private fun fillHomeLocation() {
        val homeLocation = AppDataManager.instance.personnelInfo?.homeLocation

        homeLocation?.let {
            val location = LatLng(homeLocation.latitude, homeLocation.longitude)
            googleMap?.addMarker(MarkerOptions().position(location).icon(homeIcon))
            if (viewModel.myRouteDetails.value == null && viewModel.zoomStation.not()) {
                if (viewModel.zoomStation.not()) {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
                }
            }
        }
    }

    private fun fillDestination() {
        googleMap?.addMarker(MarkerOptions().position(destinationLatLng
                ?: LatLng(0.0, 0.0)).icon(workplaceIcon))
    }

    private fun navigateToMap(userLocationLatitude: Double, userLocationLongitude: Double, targetLocationLatitude: Double, targetLocationLongitude: Double) {
        val baseUri = "geo:%s,%s?q=%s,%s"
        val uri = String.format(baseUri, userLocationLatitude, userLocationLongitude, targetLocationLatitude, targetLocationLongitude)
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e1: Exception) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(java.lang.String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", userLocationLatitude, userLocationLongitude, targetLocationLatitude, targetLocationLongitude)))
                    startActivity(browserIntent)
                } catch (e2: java.lang.Exception) {
                    AppLogger.e(e, "NavigationAppNotFound")
                    Toast.makeText(requireContext(), R.string.Maps_No_Exist, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun markerClicked(marker: Marker): Boolean {
        return if (marker.tag is StationModel) {

            val station = marker.tag as StationModel
            viewModel.selectedStation = station
            selectedStation = station
            updateSelectedStop()
            viewModel.currentRoute?.let {
                fillUI(it)
            }

            true
        } else {
            false
        }
    }

    private fun updateSelectedStop() {
        viewModel.selectedStation?.let {
            binding.textViewBottomSheetStopName.text = selectedStation?.name
        }
    }

    private fun fillUI(routeModel: RouteModel) {
        googleMap?.clear()

        val isFirstLeg = viewModel.workgroupTemplate?.fromType?.let { viewModel.workgroupTemplate?.direction?.let { it1 -> viewModel.isFirstLeg(it1, it) } } == true
        isFirstLeg.let { routeModel.getRoutePath(it) }?.data?.let { fillPath(it) }
        isFirstLeg.let { routeModel.getRoutePath(it) }?.stations?.let { fillStations(it) }

        fillDestination()
        fillHomeLocation()

        binding.textViewBottomSheetVehicleName.text = routeModel.vehicle.carInfo()

    }

    private fun fillPath(pointList: List<List<Double>>) {

        if (pointList.isNotEmpty()) {
            val options = PolylineOptions().width(10F)
            val firstPoint = pointList[0]
            val lastPoint = pointList[pointList.lastIndex]
            if (lastPoint.size == 2) {
                viewModel.workLocation = LatLng(lastPoint[0], lastPoint[1])
                destinationLatLng = LatLng(lastPoint[0], lastPoint[1])
            }
            if (firstPoint.size == 2) {
                var minLat = firstPoint[0]
                var maxLat = minLat
                var minLng = firstPoint[1]
                var maxLng = minLng
                for (point in pointList) {
                    if (point.size == 2) {
                        val lat = point[0]
                        val lng = point[1]
                        if (lat < minLat) {
                            minLat = lat
                        } else if (lat > maxLat) {
                            maxLat = lat
                        }
                        if (lng < minLng) {
                            minLng = lng
                        } else if (lng > maxLng) {
                            maxLng = lng
                        }
                        options.add(LatLng(lat, lng))
                    }
                }

                try {
                    val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng)), 100)
                    googleMap?.moveCamera(cu)
                }
                catch (e: Exception) {

                }
            }

            googleMap?.addPolyline(options)

        }
    }

    private fun fillStations(stations: List<StationModel>) {
        for (station in stations) {
            val marker: Marker? = if (station.id == selectedStation?.id) {
                googleMap?.addMarker(MarkerOptions().position(LatLng(station.location.latitude, station.location.longitude)).icon(myStationIcon))
            } else {
                googleMap?.addMarker(MarkerOptions().position(LatLng(station.location.latitude, station.location.longitude)).icon(stationIcon))
            }
            marker?.tag = station

            if(station.id == selectedStation?.id) {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(station.location.latitude, station.location.longitude), 11f))
            }

        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()

    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "ShuttleServicePlanningReservationFragment"

        fun newInstance() = ShuttleServicePlanningReservationFragment()

    }
}