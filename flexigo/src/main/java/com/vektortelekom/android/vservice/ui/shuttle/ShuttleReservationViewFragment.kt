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
import com.vektortelekom.android.vservice.ui.shuttle.map.ShuttleInfoWindowAdapter
import com.vektortelekom.android.vservice.utils.*
import java.util.*
import javax.inject.Inject

class ShuttleReservationViewFragment : BaseFragment<ShuttleViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: ReservationViewBinding

    private var destinationLatLng: LatLng? = null
    private var selectedStation: StationModel? = null

    private var googleMap: GoogleMap? = null

    private var workplaceIcon: BitmapDescriptor? = null
    private var toLocationIcon: BitmapDescriptor? = null
    private var stationIcon: BitmapDescriptor? = null
    private var myStationIcon: BitmapDescriptor? = null

    private lateinit var locationClient: FusedLocationClient

    private var lastClickedMarker : Marker? = null

    var destination : DestinationModel? = null

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

            viewModel.routeDetails.value?.let { fillUI(it) }

            googleMap?.setOnMarkerClickListener { marker ->
                markerClicked(marker)
            }
        }

        viewModel.cardCurrentRide.value?.workgroupInstanceId?.let {
            viewModel.getWorkgroupInformation(
                it
            )
        }
        
//        viewModel.routeForWorkgroup.observe(viewLifecycleOwner){ it ->
//            if (it != null){
//                if (it.template.direction == WorkgroupDirection.ROUND_TRIP) {
//                    binding.textviewDepartureTime.text = getString(R.string.departure_arrival_time)
//
//                    it.template.let {
//                        binding.textviewDepartureTimeValue.text =
//                            ((it.shift?.departureHour ?: it.shift?.arrivalHour).convertHourMinutes()
//                                ?: "") + "-" + ((it.shift?.returnDepartureHour
//                                ?: it.shift?.returnArrivalHour).convertHourMinutes() ?: "")
//                    }
//
//                } else{
//                    if (viewModel.cardCurrentRide.value?.firstLeg == true)
//                        binding.textviewDepartureTime.text = getString(R.string.arrival_time_2)
//                    else
//                        binding.textviewDepartureTime.text = getString(R.string.departure_time_4)
//
//                }
//
//            }
//        }

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
            activity?.finish()
        }

        binding.buttonCancelReservation.setOnClickListener {
//            if (viewModel.daysValues.value?.size()!! > 1)
//                showConfirmationMessage()
//            else
                cancelReservation()
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

        }
    }

    private fun fillUI(route: RouteModel){
        googleMap?.clear()

        val isFirstLeg = viewModel.cardCurrentRide.value?.fromType?.let { viewModel.cardCurrentRide.value?.workgroupDirection?.let { it1 -> viewModel.isFirstLeg(it1, it) } } == true
        isFirstLeg.let { route.getRoutePath(it) }?.data?.let { fillPath(it) }
        isFirstLeg.let { route.getRoutePath(it) }?.stations?.let { fillStations(it) }

        val minuteText = requireContext().getString(R.string.short_minute)
        val walkingDurationInMin = route.closestStation?.durationInMin?.toInt() ?: 0
        val walkingDurationInMinDisplayString = walkingDurationInMin.toString().plus(minuteText)

        binding.textViewDurationWalking.text = walkingDurationInMinDisplayString
        binding.textviewDurationTrip.text = route.durationInMin?.toString().plus(minuteText)
        binding.textviewTotalValue.text = "  ".plus("${(walkingDurationInMin) + (route.durationInMin?.toInt() ?: 0)}${minuteText}")
        binding.textviewRouteName.text = route.title

        if(route.vehicle.plateId == "" || route.vehicle.plateId == null) {
            binding.textviewPlateValue.text = getString(R.string.not_assigned)
            binding.textviewPlateValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
        }
        else{
            binding.textviewPlateValue.text = route.vehicle.plateId
            binding.textviewPlateValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkNavyBlue))
        }

        val firstDepartureDate = viewModel.cardCurrentRide.value?.firstDepartureDate
        val returnDepartureDate = viewModel.cardCurrentRide.value?.returnDepartureDate

            if (viewModel.cardCurrentRide.value?.workgroupDirection == WorkgroupDirection.ROUND_TRIP) {
                binding.textviewDepartureTime.text = getString(R.string.departure_arrival_time)

                if (firstDepartureDate != null && returnDepartureDate != null)
                    binding.textviewDepartureTimeValue.text = firstDepartureDate.convertToShuttleDateTime().plus(" - ").plus(returnDepartureDate.convertToShuttleDateTime())
                else if (firstDepartureDate != null)
                    binding.textviewDepartureTimeValue.text = firstDepartureDate.convertToShuttleDateTime()
                else if (returnDepartureDate != null)
                    binding.textviewDepartureTimeValue.text = returnDepartureDate.convertToShuttleDateTime()

            } else{
                if (viewModel.cardCurrentRide.value?.firstLeg == true)
                    binding.textviewDepartureTime.text = getString(R.string.arrival_time_2)
                else
                    binding.textviewDepartureTime.text = getString(R.string.departure_time_4)

                binding.textviewDepartureTimeValue.text = firstDepartureDate.convertToShuttleDateTime()

            }

        binding.textviewDateValue.text = firstDepartureDate.convertToShuttleDate()

        fillDestination()


    }

    private fun showConfirmationMessage(){

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setCancelable(true)
        dialog.setMessage(getString(R.string.reservations_warning_title))
        dialog.setPositiveButton(resources.getString(R.string.just_one_reservations)) { d, _ ->
            d.dismiss()
            cancelReservation()
        }
        dialog.setNegativeButton(resources.getString(R.string.all_reservations)) { d, _ ->
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
                val textMessage = if (resources.configuration.locale.language.equals("tr")){
                    getString(
                        R.string.shuttle_demand_cancel_info,
                        workgroup.firstDepartureDate.convertToShuttleReservationTime2()
                    )
                } else
                {
                    getString(
                        R.string.shuttle_demand_cancel_info,
                        longToCalendar(workgroup.firstDepartureDate)?.time?.getCustomDateStringEN(withYear = true, withComma = false).plus(", ").plus(workgroup.firstDepartureDate.convertToShuttleDateTime())
                    )
                }
                FlexigoInfoDialog.Builder(requireContext())
                    .setTitle(getString(R.string.shuttle_demand_cancel))
                    .setText1(textMessage)
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.Generic_Continue)) { dialog ->
                        dialog.dismiss()
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
                                returnLegStationId = null
                            )
                        )
                    }
                    .setCancelButton(getString(R.string.Generic_Close)) { dialog ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()


            } else if (workgroup.routeId == null) {

                FlexigoInfoDialog.Builder(requireContext())
                    .setTitle(getString(R.string.shuttle_demand_cancel))
                    .setText1(
                        getString(
                            R.string.shuttle_demand_cancel_info,
                            workgroup.firstDepartureDate.convertToShuttleReservationTime2()
                        )
                    )
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.Generic_Continue)) { dialog ->
                        dialog.dismiss()

                        viewModel.cancelDemandWorkgroup(
                            WorkgroupDemandRequest(
                                workgroupInstanceId = workgroup.workgroupInstanceId,
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
            }
        }
    }

    private fun fillDestination() {
        googleMap?.addMarker(MarkerOptions().position(destinationLatLng ?: LatLng(0.0, 0.0)).icon(workplaceIcon))?.tag
//        googleMap?.addMarker(MarkerOptions().position(LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude)).icon(toLocationIcon))?.tag = viewModel.toLabelText.value

        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng!!, 10f))
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