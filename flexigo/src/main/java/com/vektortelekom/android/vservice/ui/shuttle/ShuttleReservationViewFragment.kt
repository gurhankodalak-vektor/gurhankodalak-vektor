package com.vektortelekom.android.vservice.ui.shuttle

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.ReservationViewBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.shuttle.map.ShuttleInfoWindowAdapter
import com.vektortelekom.android.vservice.utils.*
import java.util.*
import javax.inject.Inject

class ShuttleReservationViewFragment : BaseFragment<ShuttleViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: ReservationViewBinding

    private var selectedStation: StationModel? = null

    private val markerList : MutableList<Marker> = ArrayList()
    private var googleMap: GoogleMap? = null

    private var workplaceIcon: BitmapDescriptor? = null
    private var toLocationIcon: BitmapDescriptor? = null
    private var stationIcon: BitmapDescriptor? = null
    private var myStationIcon: BitmapDescriptor? = null
    private var homeIcon: BitmapDescriptor? = null

    private lateinit var locationClient: FusedLocationClient

    private var lastClickedMarker : Marker? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<ReservationViewBinding>(inflater, R.layout.reservation_view, container, false).apply {
            lifecycleOwner = this@ShuttleReservationViewFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)

        selectedStation = viewModel.selectedStation


        binding.mapView.getMapAsync { map ->
            googleMap = map
            googleMap!!.uiSettings.isZoomControlsEnabled = true

            if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
                onLocationPermissionOk()
            }
            else {
                onLocationPermissionFailed()
            }

            googleMap?.setInfoWindowAdapter(ShuttleInfoWindowAdapter(requireActivity()))

            workplaceIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_workplace)
            toLocationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_route_to_yellow)
            stationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_station)
            myStationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_my_station_blue)
            homeIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_home)

            fillUI(viewModel.routeDetails.value)

            googleMap?.setOnMarkerClickListener { marker ->
                markerClicked(marker)
            }
        }

        viewModel.cardCurrentRide.value?.workgroupInstanceId?.let {
            viewModel.getWorkgroupInformation(
                it
            )
        }

        if (AppDataManager.instance.companySettings?.driversCanBeCalled == false) {
            binding.imageviewCall.visibility = View.GONE
        }

        binding.imageviewCall.setOnClickListener {
            val phoneNumber = viewModel.routeDetails.value?.driver?.phoneNumber

            if (phoneNumber == null)
                viewModel.navigator?.handleError(Exception(getString(R.string.error_empty_phone_number)))
            else {
                AppDialog.Builder(requireContext())
                    .setCloseButtonVisibility(false)
                    .setIconVisibility(false)
                    .setTitle(getString(R.string.call_2))
                    .setSubtitle(getString(R.string.will_call, phoneNumber))
                    .setOkButton(getString(R.string.Generic_Ok)) { d ->
                        d.dismiss()

                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(phoneNumber)))
                        startActivity(intent)

                    }
                    .setCancelButton(getString(R.string.cancel)) { d ->
                        d.dismiss()
                    }
                    .create().show()
            }
        }

        binding.imageviewBack.setOnClickListener {
            closeFragment()
        }

        binding.buttonCancelReservation.setOnClickListener {
//            if (viewModel.daysValues.value?.size()!! > 1)
//                showConfirmationMessage()
//            else
                cancelReservation()
        }

    }

    private fun closeFragment(){
        val bottomNavigation = requireActivity().findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        val layoutToolbar = requireActivity().findViewById<View>(R.id.layout_toolbar) as ConstraintLayout

        bottomNavigation.visibility = View.VISIBLE
        layoutToolbar.visibility = View.VISIBLE

        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    private fun showAllMarkers() {
        val builder = LatLngBounds.Builder()
        for (m in markerList)
            builder.include(m.position)

        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.30).toInt()

        googleMap!!.setPadding(0,0,0, binding.layoutBottomSheet.measuredHeight)

        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        googleMap!!.animateCamera(cu)

    }

    private fun fillPath(pointList: List<List<Double>>) {

        if (pointList.isNotEmpty()) {
            val options = PolylineOptions()
            val firstPoint = pointList[0]

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
                    val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng)), 120)
                    googleMap?.moveCamera(cu)
                    googleMap?.animateCamera(cu)
                }
                catch (e: Exception) {

                }
            }

            googleMap?.addPolyline(options)

        }
    }

    private fun markerClicked(marker: Marker): Boolean {
        return if (marker.tag is StationModel) {
            val station = marker.tag as StationModel
            viewModel.selectedStation = station
            lastClickedMarker?.setIcon(stationIcon)

            marker.setIcon(myStationIcon)
            lastClickedMarker = marker
            lastClickedMarker?.showInfoWindow()

            true
        } else {
            false
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
                lastClickedMarker = marker
                lastClickedMarker?.showInfoWindow()

                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(station.location.latitude, station.location.longitude), 12f)
                googleMap?.moveCamera(cu)
                googleMap?.animateCamera(cu)
            }

            if (marker != null) {
                markerList.add(marker)
            }

        }
    }

    private fun getDestinationInfo(){

        viewModel.destinations.value?.let { destinations ->
            destinations.forEachIndexed { _, destinationModel ->

                if (viewModel.cardCurrentRide.value != null && viewModel.isFromCampus){
                    if(destinationModel.id == viewModel.cardCurrentRide.value!!.fromTerminalReferenceId) {
                        destinationLatLng = LatLng(destinationModel.location!!.latitude, destinationModel.location.longitude)
                    }
                } else{
                    if(viewModel.cardCurrentRide.value != null && destinationModel.id == viewModel.cardCurrentRide.value!!.toTerminalReferenceId) {
                        destinationLatLng = LatLng(destinationModel.location!!.latitude, destinationModel.location.longitude)
                    }
                }

            }
        }

    }

    private fun fillUI(route: RouteModel?){
        googleMap?.clear()

        viewModel.cardCurrentRide.value.let { ride ->

            getDestinationInfo()
            binding.textviewRouteName.text = route?.title
            viewModel.isFromCampus = (ride?.fromType == FromToType.CAMPUS || ride?.fromType == FromToType.PERSONNEL_WORK_LOCATION) //outbound

            if (ride?.reserved == true){
                binding.buttonCancelReservation.text = getString(R.string.shuttle_reservation_cancel_button)
                binding.textviewTotal.text = getString(R.string.ett)

                binding.textviewTotal.visibility = View.VISIBLE

            } else{

                if (ride?.workgroupStatus == WorkgroupStatus.PENDING_DEMAND || ride?.workgroupStatus == WorkgroupStatus.PENDING_PLANNING){

                    binding.buttonCancelReservation.text = getString(R.string.cancel_request)
                    binding.textviewRouteName.text = getString(R.string.shuttle_request)

                    binding.textviewNoPlanningRoute.visibility = View.VISIBLE
                    binding.layoutRouteDetails.visibility = View.GONE
                    binding.textviewTotal.visibility = View.GONE
                    binding.textviewTotalValue.visibility = View.GONE
                } else{

                    binding.textviewNoPlanningRoute.visibility = View.GONE
                    binding.textviewTotal.text = getString(R.string.eta_2)
                    binding.buttonCancelReservation.text = getString(R.string.cancel_trip)

                    binding.textviewTotal.visibility = View.VISIBLE
                    binding.textviewTotalValue.visibility = View.VISIBLE
                    binding.layoutRouteDetails.visibility = View.VISIBLE
                }

            }

        }

        val isFirstLeg = viewModel.cardCurrentRide.value?.fromType?.let { viewModel.cardCurrentRide.value?.workgroupDirection?.let { it1 -> viewModel.isFirstLeg(it1, it) } } == true
        if (route != null &&
            !(viewModel.cardCurrentRide.value?.workgroupStatus == WorkgroupStatus.PENDING_DEMAND || viewModel.cardCurrentRide.value?.workgroupStatus == WorkgroupStatus.PENDING_PLANNING)) {
            isFirstLeg.let { route.getRoutePath(it) }?.data?.let { fillPath(it) }
            isFirstLeg.let { route.getRoutePath(it) }?.stations?.let { fillStations(it) }
        }

        var stationTime : Int? = null
        val minuteText = requireContext().getString(R.string.short_minute)
        val walkingDurationInMin = route?.closestStation?.durationInMin?.toInt() ?: 0
        val walkingDurationInMinDisplayString = walkingDurationInMin.toString().plus(minuteText)

        binding.textViewDurationWalking.text = walkingDurationInMinDisplayString
        binding.textviewDurationTrip.text = route?.durationInMin?.toString().plus(minuteText)
        binding.textviewTotalValue.text = "  ".plus("${(walkingDurationInMin) + (route?.durationInMin?.toInt() ?: 0)}${minuteText}")


        if(route?.vehicle?.plateId == "" || route?.vehicle?.plateId == null) {
            binding.textviewPlateValue.text = getString(R.string.not_assigned)
            binding.textviewPlateValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
        }
        else{
            binding.textviewPlateValue.text = route.vehicle.plateId
            binding.textviewPlateValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkNavyBlue))
        }

        if (viewModel.stations.value != null){
            for (station in viewModel.stations.value!!){
                if (viewModel.cardCurrentRide.value != null && viewModel.cardCurrentRide.value!!.stationId == station.id) {
                    stationTime = station.expectedArrivalHour
                }
            }
        }



        viewModel.routeForWorkgroup.observe(viewLifecycleOwner){ workgroup ->
            if (workgroup != null){

                if(viewModel.isFromCampus){

                    val timeValue = viewModel.routeForWorkgroup.value!!.template.shift?.departureHour?.toLong().convertHourMinutes(requireContext())
                        ?: viewModel.cardCurrentRide.value!!.firstDepartureDate.convertToShuttleDateTime(requireContext())

                    binding.textviewDepartureTime.text = getString(R.string.vanpool_departure_from_campus)
                    binding.textviewArrivalTime.text = getString(R.string.arrival_at_stop)

                    if (stationTime == null){
                        binding.layoutArrival.visibility = View.GONE
                        binding.viewDividerArrival.visibility = View.GONE
                    } else
                        binding.textviewArrivalTimeValue.text = stationTime.convertHourMinutes(requireContext()).toString()


                    if (timeValue == null){
                        binding.layoutDepartureTime.visibility = View.GONE
                        binding.viewDividerDepartureTime.visibility = View.GONE
                    } else
                        binding.textviewDepartureTimeValue.text = timeValue

                } else{

                    val timeValue = viewModel.routeForWorkgroup.value!!.template.shift?.arrivalHour?.toLong().convertHourMinutes(requireContext())
                        ?: viewModel.cardCurrentRide.value!!.firstDepartureDate.convertToShuttleDateTime(requireContext())

                    binding.textviewDepartureTime.text = getString(R.string.vanpool_departure_from_stop)
                    binding.textviewArrivalTime.text = getString(R.string.arrival_at_destination)

                    if (timeValue == null){
                        binding.layoutArrival.visibility = View.GONE
                        binding.viewDividerArrival.visibility = View.GONE
                    } else{
                        binding.textviewArrivalTimeValue.text = timeValue
                    }

                    if (stationTime == null){
                        binding.layoutDepartureTime.visibility = View.GONE
                        binding.viewDividerDepartureTime.visibility = View.GONE
                    } else
                        binding.textviewDepartureTimeValue.text = stationTime.convertHourMinutes(requireContext()).toString()

                }
            }
        }

        if (getString(R.string.generic_language) == "tr")
            binding.textviewDateValue.text = viewModel.cardCurrentRide.value?.firstDepartureDate.convertToShuttleDate()
        else
            binding.textviewDateValue.text = longToCalendar(viewModel.cardCurrentRide.value?.firstDepartureDate)?.time?.getCustomDateStringEN(withYear = true, withComma = true)


        fillDestination()
        showAllMarkers()

    }

    private fun showConfirmationMessage(){

        val messageText = getString(
            R.string.shuttle_demand_cancel_info_detail,
            viewModel.cardCurrentRide.value?.routeName,
            longToCalendar(viewModel.cardCurrentRide.value?.firstDepartureDate)?.time?.getCustomDateStringEN(withYear = true, withComma = true),
            viewModel.cardCurrentRide.value?.firstDepartureDate.convertToShuttleDateTime(requireContext())
        ).plus(getString(R.string.this_is_multi_day))

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setCancelable(true)
        dialog.setTitle(getString(R.string.delete_reservation))
        dialog.setMessage(messageText)
        dialog.setPositiveButton(resources.getString(R.string.delete_this_day_only)) { d, _ ->
            d.dismiss()
            cancelReservation()
        }
        dialog.setNegativeButton(resources.getString(R.string.delete_all_days)) { d, _ ->
            d.dismiss()
            // TODO: servis eklendikten sonra yapılacak
        }
        dialog.setNeutralButton(resources.getString(R.string.cancel)) { d, _ ->
            d.dismiss()
        }

        dialog.show()
    }

    private fun cancelReservation(){

        viewModel.cardCurrentRide.value?.let { workgroup ->
            if (workgroup.reserved) {
                val textMessage = if (getString(R.string.generic_language) == "tr"){
                    getString(
                        R.string.shuttle_demand_cancel_info,
                        workgroup.firstDepartureDate.convertToShuttleReservationTime2(requireContext())
                    )
                } else
                {
                    getString(
                        R.string.shuttle_demand_cancel_info_detail,
                        workgroup.routeName,
                        longToCalendar(workgroup.firstDepartureDate)?.time?.getCustomDateStringEN(withYear = true, withComma = true),
                        workgroup.firstDepartureDate.convertToShuttleDateTime(requireContext())
                    )
                }

                FlexigoInfoDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_reservation))
                    .setText1(textMessage)
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.delete)) { dialog ->
                        dialog.dismiss()

                        closeFragment()

                        val firstLeg = workgroup.firstLeg

                        viewModel.cancelShuttleReservation2(
                            request = ShuttleReservationRequest2(
                                reservationDay = Date(workgroup.firstDepartureDate).convertForBackend2(),
                                reservationDayEnd = null,
                                workgroupInstanceId = workgroup.workgroupInstanceId,
                                routeId = workgroup.routeId ?: 0,
                                useFirstLeg = if (firstLeg) false else null,
                                firstLegStationId = null,
                                useReturnLeg = if (firstLeg.not()) false else null,
                                returnLegStationId = null,
                                destinationId = workgroup.destinationId
                            )
                        )
                    }
                    .setCancelButton(getString(R.string.cancel)) { dialog ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()


            } else {

                val textMessage = if (getString(R.string.generic_language) == "tr"){
                    getString(
                        R.string.shuttle_demand_cancel_info,
                        workgroup.firstDepartureDate.convertToShuttleReservationTime2(requireContext())
                    )
                } else
                {
                    getString(
                        R.string.shuttle_demand_cancel_info_detail,
                        workgroup.routeName,
                        longToCalendar(workgroup.firstDepartureDate)?.time?.getCustomDateStringEN(withYear = true, withComma = true),
                        workgroup.firstDepartureDate.convertToShuttleDateTime(requireContext())
                    )
                }

                FlexigoInfoDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_reservation))
                    .setText1(textMessage)
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.delete)) { dialog ->
                        dialog.dismiss()

                        closeFragment()

                        viewModel.cancelDemandWorkgroup(
                            WorkgroupDemandRequest(
                                workgroupInstanceId = workgroup.workgroupInstanceId,
                                stationId = null,
                                location = null,
                                destinationId = workgroup.destinationId
                            )
                        )

                    }
                    .setCancelButton(getString(R.string.cancel)) { dialog ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }

    private var destinationLatLng: LatLng? = null

    private fun fillDestination() {

            val defaultDestination = LatLng(AppDataManager.instance.personnelInfo?.destination?.location!!.latitude, AppDataManager.instance.personnelInfo?.destination?.location!!.longitude)

            val markerDest = googleMap?.addMarker(MarkerOptions().position(destinationLatLng ?: defaultDestination).icon(workplaceIcon))

            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng ?: defaultDestination, 12f))
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng ?: defaultDestination, 12f))

            if (markerDest != null) {
                markerList.add(markerDest)
            }


        val homeLocation = AppDataManager.instance.personnelInfo?.homeLocation

        if(homeLocation != null) {

            val marker = googleMap?.addMarker(MarkerOptions().position(LatLng(homeLocation.latitude, homeLocation.longitude)).icon(homeIcon))

            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(homeLocation.latitude, homeLocation.longitude), 12f))
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(homeLocation.latitude, homeLocation.longitude), 12f))

            if (marker != null) {
                markerList.add(marker)
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
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
        const val TAG: String = "ShuttleReservationViewFragment"
        fun newInstance() = ShuttleReservationViewFragment()

    }

    override fun onLocationPermissionFailed() {
    }

    override fun onLocationPermissionOk() {
        locationClient = FusedLocationClient(requireContext())

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            @SuppressLint("MissingPermission")
            override fun onLocationUpdated(location: Location) {
                AppDataManager.instance.currentLocation = location
                googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                googleMap?.isMyLocationEnabled = true

                locationClient.stop()

            }
            override fun onLocationFailed(message: String) {

                if(activity?.isFinishing != false || activity?.isDestroyed != false) {
                    return
                }

                when (message) {
                    FusedLocationClient.ERROR_LOCATION_DISABLED -> locationClient.showLocationSettingsDialog()
                    FusedLocationClient.ERROR_LOCATION_MODE -> {
                        locationClient.showLocationSettingsDialog()
                    }
                    FusedLocationClient.ERROR_TIMEOUT_OCCURRED -> {
                        (activity as BaseActivity<*>).handleError(RuntimeException(getString(R.string.location_timeout)))
                    }
                }
            }

        })
    }
}