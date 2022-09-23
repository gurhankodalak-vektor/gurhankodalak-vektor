package com.vektortelekom.android.vservice.ui.route.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.gson.JsonArray
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.RouteSearchReservationBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.dialog.ReservationDialog
import com.vektortelekom.android.vservice.ui.route.bottomsheet.BottomSheetSingleDateCalendar
import com.vektortelekom.android.vservice.ui.shuttle.map.ShuttleInfoWindowAdapter
import com.vektortelekom.android.vservice.utils.*
import java.util.*
import javax.inject.Inject


class RouteSearchReservationFragment : BaseFragment<RouteSearchViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RouteSearchViewModel

    lateinit var binding: RouteSearchReservationBinding

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
        binding = DataBindingUtil.inflate<RouteSearchReservationBinding>(inflater, R.layout.route_search_reservation, container, false).apply {
            lifecycleOwner = this@RouteSearchReservationFragment
            viewModel = this@RouteSearchReservationFragment.viewModel
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

            viewModel.routeSelectedForReservation.value?.let { fillUI(it) }

            googleMap?.setOnMarkerClickListener { marker ->
                markerClicked(marker)
            }
            }

        val days = resources.getStringArray(R.array.weekdays_array)

        val jsonArray = JsonArray()
        for (day in days){
            jsonArray.add(day)
        }
        viewModel.weekdays.value = jsonArray

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
            NavHostFragment.findNavController(this).navigateUp()
        }

        binding.imagebuttonStartEdit.setOnClickListener {
            viewModel.mode = "startDay"
            val bottomSheetSingleDateCalendar = BottomSheetSingleDateCalendar()
            bottomSheetSingleDateCalendar.show(requireActivity().supportFragmentManager, bottomSheetSingleDateCalendar.tag)
        }

        binding.imagebuttonFinishEdit.setOnClickListener {
            viewModel.mode = "finishDay"
            val bottomSheetSingleDateCalendar = BottomSheetSingleDateCalendar()
            bottomSheetSingleDateCalendar.show(requireActivity().supportFragmentManager, bottomSheetSingleDateCalendar.tag)
        }

        binding.buttonUseRegularly.setOnClickListener {

            FlexigoInfoDialog.Builder(requireContext())
                .setTitle(getString(R.string.shuttle_change_info_title))
                .setText1(getString(R.string.shuttle_change_info_text, viewModel.routeTitle.value ?: ""))
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
        }

        binding.buttonReserve.setOnClickListener {
            viewModel.selectedDate.let { selectedDate ->
                selectedStation?.let {

                    setDepartureTime(isRoundTrip)

                    val dateString = if (viewModel.selectedStartDay.value == viewModel.selectedFinishDay.value)
                        viewModel.selectedStartDay.value.toString().plus(" ").plus(getString(R.string.on_date))
                    else
                        viewModel.selectedStartDay.value.plus(" - ").plus(viewModel.selectedFinishDay.value).plus(" ").plus(getString(R.string.between_date))


                    val weekdays = if (viewModel.daysLocalValues.value == viewModel.weekdays.value)
                        getString(R.string.all_weekdays)
                    else
                        viewModel.daysLocalValues.value.toString().replace("[","").replace("]","").replace("\"","").plus(" ").plus(getString(R.string.in_days))

                    val text = getString(R.string.shuttle_make_reservation_multiple_date_info_text,
                        dateString,
                        weekdays,
                    viewModel.routeName.value,
                    viewModel.departureArrivalTimeTextPopup.value, 
                    viewModel.routeTitle.value)

                    ReservationDialog.Builder(requireContext())
                        .setTitle(getString(R.string.reservation_confirmation))
                        .setText1(text)
                        .setText2(getString(R.string.reservation_warning))
                        .setCancelable(false)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                            viewModel.selectedStation?.let { stop ->
                                val requestModel = viewModel.getReservationRequestModel(stop)
                                requestModel?.let { model ->
                                    viewModel.makeShuttleReservation(model)
                                }

                            }
                        }
                        .setCancelButton(getString(R.string.view_reservation)) { dialog ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()

                }
            }
        }
        selectDateVisibility(false)

        binding.layoutRouteNameTimeBack.setOnClickListener {
            if (it != null)
                selectDateVisibility(true)
        }

        viewModel.updatePersonnelStationResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.updatePersonnelStationResponse.value = null

                FlexigoInfoDialog.Builder(requireContext())
                    .setText1(getString(R.string.update_personnel_station_response_dialog_text))
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

        viewModel.selectedFinishDay.observe(viewLifecycleOwner){
            if (it != null)
                selectDateVisibility(false)
        }
        viewModel.selectedStartDay.observe(viewLifecycleOwner){
            if (it != null)
                selectDateVisibility(false)
        }

        binding.textviewDepartureTimeValue.text = viewModel.selectedDate?.date.convertToShuttleDateTime()
        binding.textviewDepartureTime.text = viewModel.pickerTitle

        binding.checkboxRoundTrip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                isRoundTrip = isChecked
                setDepartureTime(isChecked)
            } else{
                isRoundTrip = false
                setDepartureTime(isChecked)
                binding.textviewDepartureTimeValue.text = viewModel.selectedDate?.date.convertToShuttleDateTime()
                binding.textviewDepartureTime.text = viewModel.pickerTitle
            }
        }

    }
    var isRoundTrip: Boolean = false
    private fun setDepartureTime(isRoundTrip: Boolean){
        var tempTime: String = ""
        var tempFirstString: String = ""
        var tempSecondString: String = ""

        val isFirstLeg = viewModel.currentWorkgroup.value?.fromType?.let { viewModel.currentWorkgroup.value?.workgroupDirection?.let { it1 -> viewModel.isFirstLeg(it1, it) } } == true
        tempTime = if (isFirstLeg){
            viewModel.currentWorkgroup.value?.returnDepartureDate.convertToShuttleDateTime()
        } else{
            viewModel.currentWorkgroup.value?.firstDepartureDate.convertToShuttleDateTime()
        }

        if (viewModel.isFromChanged.value == true){

            if (isRoundTrip)
                binding.textviewDepartureTime.text = getString(R.string.arrival_departure_time)
            else
                binding.textviewDepartureTime.text = viewModel.pickerTitle

            tempFirstString = getString(R.string.drop_off)
            tempSecondString = getString(R.string.pick_up)

        } else
        {
            if (isRoundTrip)
                binding.textviewDepartureTime.text = getString(R.string.departure_arrival_time)
            else
                binding.textviewDepartureTime.text = viewModel.pickerTitle

            tempFirstString = getString(R.string.pick_up)
            tempSecondString = getString(R.string.drop_off)

        }

        if(!isRoundTrip){
            viewModel.departureArrivalTimeText.value = viewModel.selectedDate?.date.convertToShuttleDateTime()

            binding.textviewRouteNameAndTime.text = viewModel.routeName.value.plus(", ")
                .plus(tempFirstString).plus(" ").plus(viewModel.selectedDate?.date.convertToShuttleDateTime())

            viewModel.departureArrivalTimeTextPopup.value = viewModel.selectedDate?.date.convertToShuttleDateTime().plus(" ").plus(tempFirstString)

        } else{
            viewModel.departureArrivalTimeText.value = viewModel.selectedDate?.date.convertToShuttleDateTime().plus(" - ").plus(tempTime)

            binding.textviewRouteNameAndTime.text = viewModel.routeName.value.plus(", ")
                .plus(tempFirstString).plus(" ").plus(viewModel.selectedDate?.date.convertToShuttleDateTime())
                .plus(" - ").plus(tempSecondString).plus(" ").plus(tempTime)

            viewModel.departureArrivalTimeTextPopup.value = viewModel.selectedDate?.date.convertToShuttleDateTime().plus(" ").plus(tempFirstString).plus(" , ")
                .plus(tempTime).plus(" ").plus(tempSecondString)
        }


    }

    private fun selectDateVisibility(isBack: Boolean){

        if (viewModel.selectedStartDay.value != viewModel.selectedFinishDay.value){
            binding.layoutSelectDate.visibility = View.VISIBLE
            checkBoxSelectableControl()

        } else {
            binding.layoutSelectDate.visibility = View.GONE

            val jsonArrayDate = JsonArray()
            val today = longToCalendar(viewModel.selectedStartDayCalendar.value?.time)
            jsonArrayDate.add(today?.time.convertForWeekDaysLocal())

            viewModel.daysLocalValues.value = jsonArrayDate

        }

//        if (!isBack && viewModel.selectedStartDay.value != viewModel.selectedFinishDay.value){
//
//                binding.layoutSelectDate.visibility = View.VISIBLE
//
//                binding.layoutSend.visibility = View.VISIBLE
//                binding.layoutRouteNameTimeBack.visibility = View.VISIBLE
//                binding.viewDivider0.visibility = View.VISIBLE
//
//                binding.checkboxRoundTrip.visibility = View.GONE
//                binding.layoutCheckbox.visibility = View.VISIBLE
//                binding.layoutPlate.visibility = View.GONE
//                binding.layoutDepartureTime.visibility = View.GONE
//                binding.layoutDuration.visibility = View.GONE
//                binding.layoutReservations.visibility = View.GONE
//                binding.viewDivider.visibility = View.GONE
//                binding.viewDivider4.visibility = View.GONE
//                binding.viewDivider5.visibility = View.GONE
//
//
//                setDepartureTime(isRoundTrip)
//
//        } else {
//            binding.layoutSelectDate.visibility = View.GONE
//
//            binding.layoutSend.visibility = View.GONE
//            binding.layoutRouteNameTimeBack.visibility = View.GONE
//            binding.viewDivider0.visibility = View.GONE
//
//            binding.layoutSend.visibility = View.GONE
//            binding.checkboxRoundTrip.visibility = View.VISIBLE
//            binding.layoutCheckbox.visibility = View.GONE
//            binding.layoutPlate.visibility = View.VISIBLE
//            binding.layoutDepartureTime.visibility = View.VISIBLE
//            binding.layoutDuration.visibility = View.VISIBLE
//            binding.layoutReservations.visibility = View.VISIBLE
//            binding.viewDivider.visibility = View.VISIBLE
//            binding.viewDivider4.visibility = View.VISIBLE
//            binding.viewDivider5.visibility = View.VISIBLE
//
//        }

    }

    private fun fillPath(pointList: List<List<Double>>) {

        if (pointList.isNotEmpty()) {
            val options = PolylineOptions()
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
            val stop = marker.tag as StationModel

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
                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(station.location.latitude, station.location.longitude), 14f)
                googleMap?.moveCamera(cu)
                googleMap?.animateCamera(cu)
            }

        }
    }

    private fun fillUI(route: RouteModel){
        googleMap?.clear()

        fillDestination()

        val isFirstLeg = viewModel.currentWorkgroup.value?.fromType?.let { viewModel.currentWorkgroup.value?.workgroupDirection?.let { it1 -> viewModel.isFirstLeg(it1, it) } } == true
        isFirstLeg.let { route.getRoutePath(it) }?.data?.let { fillPath(it) }
        isFirstLeg.let { route.getRoutePath(it) }?.stations?.let { fillStations(it) }

        val minuteText = requireContext().getString(R.string.short_minute)
        val walkingDurationInMin = route.closestStation?.durationInMin?.toInt() ?: 0
        val walkingDurationInMinDisplayString = walkingDurationInMin.toString().plus(minuteText)

        binding.textViewDurationWalking.text = walkingDurationInMinDisplayString
        binding.textviewDurationTrip.text = route.durationInMin?.toString().plus(minuteText)
        viewModel.routeTitle.value = route.title
        viewModel.routeName.value = route.destination.name
        binding.textviewTotalValue.text = "  ".plus("${(walkingDurationInMin) + (route.durationInMin?.toInt() ?: 0)}${minuteText}")

        if(route.vehicle.plateId == "" || route.vehicle.plateId == null) {
            binding.textviewPlateValue.text = getString(R.string.not_assigned)
            binding.textviewPlateValue.setTextColor(resources.getColor(R.color.steel))
        }
        else{
            binding.textviewPlateValue.text = route.vehicle.plateId
            binding.textviewPlateValue.setTextColor(resources.getColor(R.color.darkNavyBlue))
        }

        viewModel.departureArrivalTimeText.value  = viewModel.selectedDate?.date.convertToShuttleDateTime()

        if (viewModel.currentWorkgroupResponse.value?.template?.direction == WorkgroupDirection.ROUND_TRIP)
            binding.checkboxRoundTrip.visibility = View.VISIBLE
        else
            binding.checkboxRoundTrip.visibility = View.GONE


    }

    private fun checkBoxSelectableControl(){
        var isShorterThanSevenDays = true

        val afterSevenDays = longToCalendar(viewModel.selectedStartDayCalendar.value?.time)
        afterSevenDays?.add(Calendar.DATE, 7)

        isShorterThanSevenDays = afterSevenDays?.time!!.time >= viewModel.selectedFinishDayCalendar.value?.time!!

        val jsonArray = JsonArray()
        val jsonArrayLocal = JsonArray()
        viewModel.currentWorkgroupResponse.value?.let { it1 ->
            it1.template.shift.let {
                if (it != null) {

                    binding.layoutCheckbox.removeAllViews()
                    for (day in DAYS.values()){

                        val checkbox = layoutInflater.inflate(R.layout.checkbox, requireView().parent.parent as ViewGroup, false) as MaterialCheckBox
                        checkbox.text = day.getLocal(requireContext())
                        checkbox.id = View.generateViewId()
                        var isDateTrue = viewModel.checkIncludesRecurringDay(it, day.name)

                        if (isShorterThanSevenDays){
                            isDateTrue = isDateTrue == true && setDaysBetweenDates().contains(day.name.lowercase())
                        }

                        checkbox.isChecked = isDateTrue
                        checkbox.isEnabled = isDateTrue
                        checkbox.tag = day.ordinal

                        if (checkbox.isChecked) {
                            jsonArray.add(day.name.uppercase())
                            jsonArrayLocal.add(day.getLocalLong(requireContext()))

                        }

                        viewModel.daysValues.value = jsonArray
                        viewModel.daysLocalValues.value = jsonArrayLocal

                        checkbox.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked){
                                if (!jsonArray.toString().contains(day.name.uppercase())) {
                                    jsonArray.add(day.name.uppercase())
                                    jsonArrayLocal.add(day.getLocalLong(requireContext()))

                                }

                                viewModel.daysValues.value = jsonArray
                                viewModel.daysLocalValues.value = jsonArrayLocal
                            } else{
                                for (i in 0 until jsonArray.size() - 1){
                                    if (jsonArray.get(i).asString == day.name.uppercase()) {
                                        jsonArray.remove(i)
                                        jsonArrayLocal.remove(i)
                                    }
                                }

                                viewModel.daysValues.value = jsonArray
                                viewModel.daysLocalValues.value = jsonArrayLocal
                            }
                        }

                        binding.layoutCheckbox.addView(checkbox)
                    }


                }
            }
        }

    }

    private fun setDaysBetweenDates() : ArrayList<String>{
        val days: ArrayList<String> = ArrayList()

        val dayOfTheTime = longToCalendar(viewModel.selectedStartDayCalendar.value?.time)
        days.add(dayOfTheTime?.time.convertForWeekDaysLiteral().lowercase())

        do {
            dayOfTheTime?.add(Calendar.DATE, 1)
            if (dayOfTheTime != null) {
                days.add(dayOfTheTime.time.convertForWeekDaysLiteral().lowercase())
            }
        } while (dayOfTheTime?.time?.time!! < viewModel.selectedFinishDayCalendar.value?.time!!)


        return days
    }

    enum class DAYS(private val labelId: Int, private val labelIdLong: Int) {
        MONDAY(R.string.material_calendar_monday, R.string.monday),
        TUESDAY(R.string.material_calendar_tuesday, R.string.tuesday),
        WEDNESDAY(R.string.material_calendar_wednesday, R.string.wednesday),
        THURSDAY(R.string.material_calendar_thursday, R.string.thursday),
        FRIDAY(R.string.material_calendar_friday, R.string.friday),
        SATURDAY(R.string.material_calendar_saturday, R.string.saturday),
        SUNDAY(R.string.material_calendar_sunday, R.string.sunday);

        fun getLocal(context: Context) = context.getString(labelId)
        fun getLocalLong(context: Context) = context.getString(labelIdLong)
    }

    private fun fillDestination() {
        googleMap?.addMarker(MarkerOptions().position(destinationLatLng ?: LatLng(0.0, 0.0)).icon(workplaceIcon))?.tag = viewModel.fromLabelText.value
        googleMap?.addMarker(MarkerOptions().position(LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude)).icon(toLocationIcon))?.tag = viewModel.toLabelText.value
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
        const val TAG: String = "RouteSearchReservation"
        fun newInstance() = RouteSearchReservationFragment()

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