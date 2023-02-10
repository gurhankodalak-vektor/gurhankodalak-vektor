package com.vektortelekom.android.vservice.ui.shuttle.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.ShuttleReservationRequest
import com.vektortelekom.android.vservice.data.model.StationModel
import com.vektortelekom.android.vservice.databinding.ShuttleFromToMapFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.base.HighlightView
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.shuttle.dialog.ShuttleReservationDialog
import com.vektortelekom.android.vservice.ui.shuttle.dialog.ShuttleStationChangeDialog
import com.vektortelekom.android.vservice.utils.*
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject

class ShuttleFromToMapFragment : BaseFragment<ShuttleViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: ShuttleFromToMapFragmentBinding

    private var googleMap: GoogleMap? = null

    private var fromPinIcon: BitmapDescriptor? = null
    private var toPinIcon: BitmapDescriptor? = null
    private var stationIcon: BitmapDescriptor? = null
    private var myStationIcon: BitmapDescriptor? = null

    private var fromMarker: Marker? = null
    private var toMarker: Marker? = null

    private var prevStationMarker: Marker? = null

    @Volatile
    private var myLocation: Location? = null
    private var locationClient: FusedLocationClient? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var destinationLatLng: LatLng? = null

    private var polyline: Polyline? = null

    private var stationMarkers: MutableList<Marker>? = null

    private var isFirstExpand = false

    private var shuttleReservationDialog: ShuttleReservationDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<ShuttleFromToMapFragmentBinding>(inflater, R.layout.shuttle_from_to_map_fragment, container, false).apply {
            lifecycleOwner = this@ShuttleFromToMapFragment
            viewModel = this@ShuttleFromToMapFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync {
            googleMap = it

            if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
                onLocationPermissionOk()
            }

            fromPinIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_pin_purple)
            toPinIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_pin_marigold)
            stationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_station)
            myStationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_my_station)
            //workplaceIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_workplace)

            val fromPlace = viewModel.fromPlace.value

            if (fromPlace != null) {
                fromMarker = googleMap?.addMarker(MarkerOptions().position(fromPlace.latLng).icon(fromPinIcon))

                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(fromPlace.latLng, 14f))

                binding.textViewFrom.text = fromPlace.name
            }

            val toPlace = viewModel.toPlace.value

            if (toPlace != null) {
                toMarker = googleMap?.addMarker(MarkerOptions().position(toPlace.latLng).icon(toPinIcon))

                binding.textViewTo.text = toPlace.name
            }

            googleMap?.setOnMarkerClickListener { marker ->

                markerClicked(marker)

            }


        }

        viewModel.stopDetailsResponse.observe(viewLifecycleOwner) { response ->

            binding.cardViewStationDetails.visibility = View.VISIBLE

            polyline?.remove()
            polyline = null

            binding.cardViewStationDetailsMore.visibility = View.GONE

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            if (prevStationMarker?.tag is StationModel) {
                val station = prevStationMarker?.tag as StationModel
                binding.textViewStationName.text = station.name
                binding.textViewStationDetail.text = response.response.name
                binding.textViewStationSeat.text = getString(
                    R.string.seat,
                    response.response.personnelCount,
                    response.response.vehicleCapacity
                )
                binding.textViewStationTime.text = getString(
                    R.string.departure,
                    response.response.shift.startArrival.convertHourMinutes(requireContext())
                )
                binding.textViewStationArrival.text = getString(
                    R.string.arrival,
                    response.response.shift.startHour.convertHourMinutes(requireContext())
                )

                binding.textViewStationDetail2.text = response.response.name
                binding.textViewStationSeat2.text = getString(
                    R.string.seat,
                    response.response.personnelCount,
                    response.response.vehicleCapacity
                )
                binding.textViewStationTime2.text = getString(
                    R.string.departure,
                    response.response.shift.startArrival.convertHourMinutes(requireContext())
                )
                binding.textViewStationArrival2.text = getString(
                    R.string.arrival,
                    response.response.shift.startHour.convertHourMinutes(requireContext())
                )
            }

        }

        viewModel.fromPlace.observe(viewLifecycleOwner, Observer { fromPlace ->

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            prevStationMarker = null
            polyline?.remove()
            polyline = null

            if (fromPlace == null) {
                binding.textViewFrom.text = ""
                if (stationMarkers != null) {
                    for (marker in stationMarkers!!) {
                        marker.remove()
                    }
                }
                stationMarkers = mutableListOf()
                fromMarker?.remove()
                fromMarker = null
                return@Observer
            }

            if (googleMap != null) {

                if (fromMarker == null) {
                    fromMarker = googleMap?.addMarker(MarkerOptions().position(fromPlace.latLng).icon(fromPinIcon))
                } else {
                    fromMarker?.position = fromPlace.latLng
                }

                binding.textViewFrom.text = fromPlace.name


                if (viewModel.toPlace.value != null) {

                    /*val request = RouteStopRequest(SearchRequestModel(fromPlace.latLng.latitude, fromPlace.latLng.longitude, fromPlace.id), SearchRequestModel(viewModel.toPlace.value!!.latLng.latitude, viewModel.toPlace.value!!.latLng.longitude, viewModel.toPlace.value!!.id), null)

                    viewModel.getStops(request)
                     */

                }

            }
        })

        viewModel.toPlace.observe(viewLifecycleOwner, Observer { toPlace ->

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            prevStationMarker = null
            polyline?.remove()
            polyline = null

            if (toPlace == null) {
                binding.textViewTo.text = ""
                if (stationMarkers != null) {
                    for (marker in stationMarkers!!) {
                        marker.remove()
                    }
                }
                stationMarkers = mutableListOf()
                toMarker?.remove()
                toMarker = null
                return@Observer
            }

            if (googleMap != null) {

                if (toMarker == null) {
                    toMarker = googleMap?.addMarker(MarkerOptions().position(toPlace.latLng).icon(toPinIcon))
                } else {
                    toMarker?.position = toPlace.latLng
                }

                binding.textViewTo.text = toPlace.name

                if (viewModel.fromPlace.value != null) {

                    /*val request = RouteStopRequest(SearchRequestModel(viewModel.fromPlace.value!!.latLng.latitude, viewModel.fromPlace.value!!.latLng.longitude, viewModel.fromPlace.value!!.id), SearchRequestModel(viewModel.toPlace.value!!.latLng.latitude, viewModel.toPlace.value!!.latLng.longitude, viewModel.toPlace.value!!.id), null)

                    viewModel.getStops(request)

                     */

                }

            }
        })

        viewModel.getStopsResponse.observe(viewLifecycleOwner, Observer { response ->

            if(response != null) {
                val unCampusPlace = (if (viewModel.fromPlace.value?.isCampus == false) {
                    viewModel.fromPlace.value
                } else {
                    viewModel.toPlace.value
                })
                        ?: return@Observer

                val unCampusLocation = Location("")
                unCampusLocation.latitude = unCampusPlace.latLng.latitude
                unCampusLocation.longitude = unCampusPlace.latLng.longitude


                var minLat = unCampusPlace.latLng.latitude
                var minLng = unCampusPlace.latLng.longitude
                var maxLat = unCampusPlace.latLng.latitude
                var maxLng = unCampusPlace.latLng.longitude
                var minDistance: Float? = null
                var nearestStation: StationModel? = null

                stationMarkers = mutableListOf()

                if (googleMap != null) {

                    googleMap?.clear()
                    clearAllMarkers()

                    val fromPlace = viewModel.fromPlace.value

                    if (fromPlace != null) {
                        fromMarker = googleMap?.addMarker(MarkerOptions().position(fromPlace.latLng).icon(fromPinIcon))
                    }

                    val toPlace = viewModel.toPlace.value

                    if (toPlace != null) {
                        toMarker = googleMap?.addMarker(MarkerOptions().position(toPlace.latLng).icon(toPinIcon))
                    }

                    if(response.response.isEmpty()) {
                        FlexigoInfoDialog.Builder(requireContext())
                                .setIconVisibility(false)
                                .setCancelable(true)
                                .setTitle(getString(R.string.stops_empty_title))
                                .setText1(getString(R.string.stops_empty_text))
                                .setOkButton(getString(R.string.Generic_Ok)) {
                                    it.dismiss()
                                }
                                .create()
                                .show()
                    }

                    for (station in response.response) {
                        val marker = googleMap?.addMarker(MarkerOptions().position(LatLng(station.location.latitude, station.location.longitude)).icon(stationIcon))
                        marker?.tag = station
                        marker?.let { stationMarkers?.add(it) }

                        val stationLocation = Location("")
                        stationLocation.latitude = station.location.latitude
                        stationLocation.longitude = station.location.longitude


                        val currentDistance = unCampusLocation.distanceTo(stationLocation)

                        if(minDistance == null || minDistance > currentDistance) {
                            nearestStation = station
                            minDistance = currentDistance
                        }

                    }

                    nearestStation?.let {

                        if(minLat > it.location.latitude) {
                            minLat = it.location.latitude
                        }
                        if(minLng > it.location.longitude) {
                            minLng = it.location.longitude
                        }
                        if(maxLat < it.location.latitude) {
                            maxLat = it.location.latitude
                        }
                        if(maxLng < it.location.longitude) {
                            maxLng = it.location.longitude
                        }

                    }



                    val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng)), 100)
                    googleMap?.animateCamera(cu)

                }

                if(AppDataManager.instance.isHighlightAlreadyShown("shuttle_stops_marker").not()) {
                    binding.imageViewMarkerTutorial.post {
                        HighlightView.Builder(requireContext(), binding.imageViewMarkerTutorial, requireActivity(), "shuttle_stops_marker", "sequence_shuttle_from_to")
                                .setHighlightText(getString(R.string.tutorial_shuttle_closest_stop))
                                .addPreActionListener({
                                    binding.imageViewMarkerTutorial.visibility = View.VISIBLE
                                }, binding.imageViewMarkerTutorial)
                                .addGotItListener {
                                    binding.imageViewMarkerTutorial.visibility = View.GONE
                                }
                                .addSkipButtonListener {
                                    binding.imageViewMarkerTutorial.visibility = View.GONE
                                }
                                .create()
                    }
                }

                if(viewModel.fromPlace.value != null && viewModel.toPlace.value != null) {
                    viewModel.navigator?.setRouteFilterVisibility(true)
                }

                viewModel.getStopsResponse.value =  null
            }

        })

        viewModel.updatePersonnelStationResponse.observe(viewLifecycleOwner) {
            if (it == true) {

                val dialog = AppDialog.Builder(requireContext())
                    .setIconVisibility(true)
                    .setTitle(R.string.stop_updated_successfully)
                    .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                        dialog.dismiss()
                    }
                    .create()

                dialog.show()

                viewModel.updatePersonnelStationResponse.value = null
            }

        }

        binding.cardViewStationDetails.setOnClickListener {
            showStationDetails()
        }

        binding.layoutDirections.setOnClickListener {

            if (prevStationMarker?.tag is StationModel) {
                val station = prevStationMarker?.tag as StationModel

                navigateToMap(myLocation?.latitude ?: 0.0, myLocation?.longitude
                        ?: 0.0, station.location.latitude, station.location.longitude)

            }

        }

        binding.layoutStationUse.setOnClickListener {

            if (prevStationMarker?.tag is StationModel) {

                val stationId = (prevStationMarker?.tag as StationModel).id

                val dialog = ShuttleStationChangeDialog(requireContext(), viewModel.stopDetailsResponse.value?.response?.name
                        ?: "", object : ShuttleStationChangeDialog.ChangeSubmitListener {
                    override fun submit() {
                        viewModel.updatePersonnelStation(stationId)
                    }

                })
                dialog.show()

            }

        }

        binding.layoutCall.setOnClickListener {

            val phoneNumber = viewModel.stopDetailsResponse.value?.response?.driver?.phoneNumber

            if (phoneNumber == null) {
                viewModel.navigator?.handleError(Exception(getString(R.string.error_empty_phone_number)))
            } else {
                AppDialog.Builder(requireContext())
                        .setCloseButtonVisibility(false)
                        .setIconVisibility(false)
                        .setTitle(getString(R.string.call_2))
                        .setSubtitle(getString(R.string.will_call, phoneNumber))
                        .setOkButton(getString(R.string.Generic_Ok)) { d ->
                            d.dismiss()
                            val phoneNumber = viewModel.stopDetailsResponse.value?.response?.driver?.phoneNumber

                            if (phoneNumber == null) {
                                viewModel.navigator?.handleError(Exception(getString(R.string.error_empty_phone_number)))
                            } else {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(phoneNumber)))
                                startActivity(intent)
                            }
                        }
                        .setCancelButton(getString(R.string.cancel)) { d ->
                            d.dismiss()
                        }
                        .create().show()
            }

        }

        binding.layoutBooking.setOnClickListener {

            showShuttleReservationDialog()


        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetStopDetails)

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    if(isFirstExpand.not()) {
                        isFirstExpand = true
                        HighlightView.Builder(requireContext(), binding.cardViewStationDetails, requireActivity(), "shuttle_station_details", "sequence_shuttle_from_to")
                                .setHighlightText(getString(R.string.tutorial_shuttle_route_details))
                                .create()
                    }
                }
            }

        })

        binding.buttonMyLocation.setOnClickListener {
            myLocation?.let {
                val cu = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                googleMap?.animateCamera(cu)
            }

        }

        viewModel.reservationAdded.observe(viewLifecycleOwner) {
            if(it != null) {
                viewModel.reservationAdded.value = null
                if(shuttleReservationDialog?.isShowing == true) {
                    shuttleReservationDialog?.dismiss()
                    shuttleReservationDialog = null
                }
                FlexigoInfoDialog.Builder(requireContext())
                        .setIconVisibility(false)
                        .setTitle(getString(R.string.shuttle_reservation_dialog_title))
                        .setText1(getString(R.string.shuttle_reservation_dialog_text))
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog1 ->
                            dialog1.dismiss()

                        }
                        .create().show()
            }
        }


        binding.buttonSelectSelect.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
            viewModel.selectedShift = viewModel.shifts.value?.get(binding.numberPicker.value)
            viewModel.selectedShiftIndex = binding.numberPicker.value
            if(viewModel.fromPlace.value == null || viewModel.toPlace.value == null) {
                return@setOnClickListener
            }
            /*val request = RouteStopRequest(SearchRequestModel(viewModel.fromPlace.value!!.latLng.latitude, viewModel.fromPlace.value!!.latLng.longitude, viewModel.fromPlace.value!!.id), SearchRequestModel(viewModel.toPlace.value!!.latLng.latitude, viewModel.toPlace.value!!.latLng.longitude, viewModel.toPlace.value!!.id), viewModel.selectedShift?.id)

            viewModel.getStops(request)*/
        }

        binding.buttonSelectCancel.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
        }

        viewModel.shifts.observe(viewLifecycleOwner) {
            if(it == null) {
                return@observe
            }
            showFilter()
        }

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

    override fun onLocationPermissionOk() {
        locationClient = FusedLocationClient(requireContext())

        locationClient?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient?.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            @SuppressLint("MissingPermission")
            override fun onLocationUpdated(location: Location) {

                googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                googleMap?.isMyLocationEnabled = true

                AppDataManager.instance.currentLocation = location
                binding.buttonMyLocation.visibility = View.VISIBLE

                myLocation = location
                viewModel.myLocation = location

            }

            override fun onLocationFailed(message: String) {
                if (activity?.isFinishing != false || activity?.isDestroyed != false) {
                    return
                }

                when (message) {
                    FusedLocationClient.ERROR_LOCATION_DISABLED -> locationClient?.showLocationSettingsDialog()
                    FusedLocationClient.ERROR_LOCATION_MODE -> {
                        locationClient?.showLocationSettingsDialog()
                    }
                    FusedLocationClient.ERROR_TIMEOUT_OCCURRED -> {
                        (activity as BaseActivity<*>).handleError(RuntimeException(getString(R.string.location_timeout)))
                    }
                }
            }

        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

    override fun onLocationPermissionFailed() {
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        locationClient?.stop()
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
        const val TAG: String = "ShuttleFromToMapFragment"

        fun newInstance() = ShuttleFromToMapFragment()

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
                val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng)), 100)
                googleMap?.animateCamera(cu)
            }

            polyline = googleMap?.addPolyline(options)

        }
    }

    private fun fillStations(stations: List<StationModel>) {
        val myStation = AppDataManager.instance.personnelInfo?.station
        for (station in stations) {
            val marker: Marker? = if (station.id == myStation?.id) {
                googleMap?.addMarker(MarkerOptions().position(LatLng(station.location.latitude, station.location.longitude)).icon(myStationIcon))
            } else {
                googleMap?.addMarker(MarkerOptions().position(LatLng(station.location.latitude, station.location.longitude)).icon(stationIcon))
            }
            marker?.tag = station
            marker?.let { stationMarkers?.add(it) }
        }
    }

    private fun markerClicked(marker: Marker): Boolean {
        if (marker.tag is StationModel) {
            if (prevStationMarker != null) {
                prevStationMarker?.setIcon(stationIcon)
            }

            marker.setIcon(myStationIcon)

            prevStationMarker = marker

            /*val request = RouteStopRequest(SearchRequestModel(viewModel.fromPlace.value!!.latLng.latitude, viewModel.fromPlace.value!!.latLng.longitude, viewModel.fromPlace.value!!.id), SearchRequestModel(viewModel.toPlace.value!!.latLng.latitude, viewModel.toPlace.value!!.latLng.longitude, viewModel.toPlace.value!!.id), null)


            viewModel.getStopDetails(station.routeId, request)

             */


            return true
        } else {
            return false
        }
    }

    private fun showStationDetails() {
        binding.cardViewStationDetails.visibility = View.GONE

        val isFirstLeg = viewModel.isFirstLeg(viewModel.currentRide?.workgroupDirection!!, viewModel.currentRide?.fromType!!)
        viewModel.stopDetailsResponse.value?.response?.getRoutePath(isFirstLeg)!!.data.let { fillPath(it) }
        viewModel.stopDetailsResponse.value?.response?.getRoutePath(isFirstLeg)!!.stations.let { fillStations(it) }

        binding.cardViewStationDetailsMore.visibility = View.VISIBLE

        binding.cardViewStationDetailsMore.post {
            HighlightView.Builder(requireContext(), binding.layoutStationDetailsButtons, requireActivity(), "shuttle_station_details_buttons", "sequence_shuttle_from_to")
                    .setHighlightText(getString(R.string.tutorial_shuttle_details_buttons))
                    .addGotItListener {

                    }
                    .create()
        }

    }

    private fun showShuttleReservationDialog() {
        shuttleReservationDialog = ShuttleReservationDialog(requireContext(), viewModel.stopDetailsResponse.value?.response?.name
                ?: "", viewModel.shuttleReservationDate?: Date(),object : ShuttleReservationDialog.ShuttleReservationListener {
            override fun selectDate(dialog: ShuttleReservationDialog) {

                dialog.dismiss()

                val dateTime = DateTime((viewModel.shuttleReservationDate?:Date()).time)

                val mYear = dateTime.year
                val mMonth = dateTime.monthOfYear - 1
                val mDay = dateTime.dayOfMonth


                val datePickerDialog = DatePickerDialog.newInstance({ _, year, month, dayOfMonth ->

                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth, 0, 0, 0)
                    viewModel.shuttleReservationDate = Date(cal.timeInMillis)

                }, mYear, mMonth, mDay)
                datePickerDialog.setOnDismissListener {
                    showShuttleReservationDialog()
                }

                datePickerDialog.minDate = Calendar.getInstance()

                datePickerDialog.show(childFragmentManager, "DatePickerDialog")


            }

             override fun makeReservation(isMorning: Boolean) {

                 if (prevStationMarker?.tag is StationModel) {

                     val stationId = (prevStationMarker?.tag as StationModel).id
                     val routeId = (prevStationMarker?.tag as StationModel).routeId

                     val request = ShuttleReservationRequest(
                             bookingDay = (viewModel.shuttleReservationDate?:Date()).convertForBackend2(),
                             isIncoming = isMorning,
                             isOutgoing = isMorning.not(),
                             routeId = routeId,
                             stationId = stationId
                     )
                     viewModel.makeShuttleReservation(request)

                 }


             }

         })
        shuttleReservationDialog?.show()
    }

    private fun showFilter() {
        val shifts = viewModel.shifts.value

        if(shifts != null && shifts.isNotEmpty()) {

            val displayedValues = Array(shifts.size) { "" }
            for(i in shifts.indices) {
                displayedValues[i] = shifts[i].name
            }

            binding.numberPicker.value = 0
            binding.numberPicker.displayedValues = null
            binding.numberPicker.minValue = 0
            binding.numberPicker.maxValue = shifts.size-1
            binding.numberPicker.displayedValues = displayedValues
            binding.numberPicker.value = viewModel.selectedShiftIndex?:0
            binding.numberPicker.wrapSelectorWheel = true
            binding.textViewSelectTitle.text = getString(R.string.shift)

            binding.layoutSelect.visibility = View.VISIBLE

        }
        else {
            viewModel.navigator?.handleError(java.lang.Exception(getString(R.string.warning_select_park_empty)))
        }
    }

    private fun clearAllMarkers() {
        fromMarker = null
        toMarker = null
        prevStationMarker = null
        stationMarkers?.clear()
    }


}