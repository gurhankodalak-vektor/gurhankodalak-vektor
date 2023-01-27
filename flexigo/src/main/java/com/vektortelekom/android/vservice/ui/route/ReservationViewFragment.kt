package com.vektortelekom.android.vservice.ui.route

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
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
import com.vektortelekom.android.vservice.ui.route.search.RouteSearchViewModel
import com.vektortelekom.android.vservice.ui.shuttle.map.ShuttleInfoWindowAdapter
import com.vektortelekom.android.vservice.utils.*
import java.util.*
import javax.inject.Inject

class ReservationViewFragment : BaseFragment<RouteSearchViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RouteSearchViewModel

    lateinit var binding: ReservationViewBinding

    private var destinationLatLng: LatLng? = null
    private var selectedStation: StationModel? = null

    private var googleMap: GoogleMap? = null

    private var workplaceIcon: BitmapDescriptor? = null
    private var toLocationIcon: BitmapDescriptor? = null
    private var homeIcon: BitmapDescriptor? = null
    private var stationIcon: BitmapDescriptor? = null
    private var myStationIcon: BitmapDescriptor? = null

    private lateinit var locationClient: FusedLocationClient

    private var lastClickedMarker : Marker? = null

    var destination : DestinationModel? = null

    private val markerList : MutableList<Marker> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<ReservationViewBinding>(inflater, R.layout.reservation_view, container, false).apply {
            lifecycleOwner = this@ReservationViewFragment
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
            homeIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_home)
            stationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_station)
            myStationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_my_station_blue)

            viewModel.routeSelectedForReservation.value?.let { fillUI(it) }

            googleMap?.setOnMarkerClickListener { marker ->
                markerClicked(marker)
            }
        }
        viewModel.reservationCancelled.observe(viewLifecycleOwner) {
            if (it != null) {

                FlexigoInfoDialog.Builder(requireContext())
                    .setTitle(getString(R.string.shuttle_reservation_success_title))
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                        dialog.dismiss()
                        activity?.finish()
                    }
                    .create()
                    .show()

            }
        }

        binding.imageviewCall.setOnClickListener {
            val phoneNumber = viewModel.routeSelectedForReservation.value?.driver?.phoneNumber

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
            activity?.finish()
        }

        binding.buttonCancelReservation.setOnClickListener {
            if (viewModel.daysValues.value?.size()!! > 1)
                showConfirmationMessage()
            else
                cancelReservation()
        }

    }

    private fun showAllMarkers() {
        val builder = LatLngBounds.Builder()
        for (m in markerList)
            builder.include(m.position)

        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.30).toInt()

        // Zoom and animate the google map to show all markers
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        googleMap!!.animateCamera(cu)

        googleMap!!.setPadding(0,0,0, binding.layoutBottomSheet.measuredHeight)
    }

    private fun cancelReservation(){

        viewModel.currentWorkgroup.value?.let { workgroup ->
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
                        val firstLeg = workgroup.firstLeg

                        viewModel.cancelShuttleReservation(
                            request = ShuttleReservationRequest2(
                                reservationDay = Date(workgroup.firstDepartureDate).convertForBackend2(),
                                reservationDayEnd = null,
                                workgroupInstanceId = workgroup.workgroupInstanceId,
                                routeId = workgroup.routeId ?: 0,
                                useFirstLeg = if (firstLeg) false else null,
                                firstLegStationId = null,
                                useReturnLeg = if (firstLeg.not()) false else null,
                                returnLegStationId = null
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

                        viewModel.cancelShuttleDemand(
                            WorkgroupDemandRequest(
                                workgroupInstanceId = workgroup.workgroupInstanceId,
                                stationId = null,
                                location = null
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

    private fun fillPath(pointList: List<List<Double>>) {

        if (pointList.isNotEmpty()) {
            val options = PolylineOptions()
            val firstPoint = pointList[0]
            val lastPoint = pointList[pointList.lastIndex]
            if (lastPoint.size == 2) {
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
                    val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng)), 150)
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
                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(station.location.latitude, station.location.longitude), 12f)
                googleMap?.moveCamera(cu)
                googleMap?.animateCamera(cu)
            }
            if (marker != null) {
                markerList.add(marker)
            }
        }
    }

    private fun fillUI(route: RouteModel){
        googleMap?.clear()

        val isFirstLeg = viewModel.currentWorkgroup.value?.fromType?.let { viewModel.currentWorkgroup.value?.workgroupDirection?.let { it1 -> viewModel.isFirstLeg(it1, it) } } == true
        isFirstLeg.let { route.getRoutePath(it) }?.data?.let { fillPath(it) }
        isFirstLeg.let { route.getRoutePath(it) }?.stations?.let { fillStations(it) }

        val minuteText = requireContext().getString(R.string.short_minute)
        val walkingDurationInMin = route.closestStation?.durationInMin?.toInt() ?: 0
        val walkingDurationInMinDisplayString = walkingDurationInMin.toString().plus(minuteText)

        binding.textViewDurationWalking.text = walkingDurationInMinDisplayString
        binding.textviewDurationTrip.text = String.format("%.1f", route.durationInMin ?: 0.0).plus(minuteText)
        binding.textviewRouteName.text = route.title
        binding.textviewTotalValue.text = "  ".plus("${(walkingDurationInMin) + (route.durationInMin?.toInt() ?: 0)}${minuteText}")

        if(route.vehicle.plateId == "" || route.vehicle.plateId == null) {
            binding.textviewPlateValue.text = getString(R.string.not_assigned)
            binding.textviewPlateValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
        }
        else{
            binding.textviewPlateValue.text = route.vehicle.plateId
            binding.textviewPlateValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkNavyBlue))
        }

        binding.textviewDepartureTimeValue.text  = viewModel.departureArrivalTimeText.value
        binding.layoutArrival.visibility = View.GONE
        binding.viewDividerArrival.visibility = View.GONE

        if (getString(R.string.generic_language) == "tr"){
            if (viewModel.daysValues.value?.size()!! > 1)
                binding.textviewDateValue.text = viewModel.selectedStartDay.value.plus(" - ").plus(viewModel.selectedFinishDay.value)
            else
                binding.textviewDateValue.text = viewModel.selectedStartDay.value
        } else{
            if (viewModel.daysValues.value?.size()!! > 1)
                binding.textviewDateValue.text = viewModel.selectedStartDayCalendar.value?.getCustomDateStringEN(withYear = false, withComma = false).plus(" - ").plus(viewModel.selectedFinishDayCalendar.value?.getCustomDateStringEN(withYear = true, withComma = true).plus(" "))
            else
                binding.textviewDateValue.text = viewModel.selectedStartDayCalendar.value?.getCustomDateStringEN(withYear = true, withComma = true)

        }


        fillDestination()

        showAllMarkers()
    }

    private fun fillDestination() {
        val marker: Marker?

        val markerDest = googleMap?.addMarker(MarkerOptions().position(destinationLatLng ?: LatLng(0.0, 0.0)).icon(workplaceIcon))
        markerDest?.tag = viewModel.fromLabelText.value

        if (markerDest != null) {
            markerList.add(markerDest)
        }

        if (viewModel.isLocationToHome.value == true) {
            marker = googleMap?.addMarker(MarkerOptions().position(LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude)).icon(homeIcon))
            marker?.tag = viewModel.toLabelText.value
        }
        else {
            marker = googleMap?.addMarker(MarkerOptions().position(LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude)).icon(toLocationIcon))
            marker?.tag = viewModel.toLabelText.value
        }
        if (marker != null) {
            markerList.add(marker)
        }

        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude), 10f))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng ?: LatLng(0.0, 0.0), 10f))
    }


    private fun showConfirmationMessage(){

        val messageText = getString(
            R.string.shuttle_demand_cancel_info_detail,
            viewModel.routeSelectedForReservation.value?.title,
            binding.textviewDateValue.text,
            viewModel.currentWorkgroup.value?.firstDepartureDate.convertToShuttleDateTime(requireContext())
        ).plus(getString(R.string.this_is_multi_day))

        val title = getString(R.string.delete_reservation)

        AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
            .setTitle(fromHtml("<b>$title</b>"))
            .setMessage(messageText)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.delete_all_days)) { d, _ ->
                d.dismiss()
            // TODO: servis eklendikten sonra yapılacak
            }
            .setNegativeButton(getString(R.string.delete_this_day_only)) { d, _ ->
                d.dismiss()
                cancelReservation()
            }
            .setNeutralButton(getString(R.string.cancel)) { d, _ ->
                d.dismiss()
            }
            .create()
            .show()

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

    override fun getViewModel(): RouteSearchViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[RouteSearchViewModel::class.java] }
                ?: throw Exception("Invalid Activity")

        return viewModel

    }

    companion object {
        const val TAG: String = "ReservationViewFragment"
        fun newInstance() = ReservationViewFragment()

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