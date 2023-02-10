package com.vektortelekom.android.vservice.ui.route.search

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
    private var homeIcon: BitmapDescriptor? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
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
            homeIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_home)

            viewModel.routeSelectedForReservation.value?.let { fillUI(it) }

            googleMap?.setOnMarkerClickListener { marker ->
                markerClicked(marker)
            }
        }


        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetBehavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels / 5

                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {}
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        showAllMarkers(false)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        showAllMarkers(true)
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {}
                    BottomSheetBehavior.STATE_SETTLING -> {}
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {}
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })

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
            if (viewModel.routeSelectedForReservation.value?.personnelCount!! < viewModel.routeSelectedForReservation.value?.vehicleCapacity!!){
                FlexigoInfoDialog.Builder(requireContext())
                    .setTitle(getString(R.string.shuttle_change_info_title))
                    .setText1(getString(R.string.shuttle_change_info_text, viewModel.routeTitle.value ?: ""))
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.confirm_change)) { dialog ->
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

        binding.buttonReserve.setOnClickListener {
            if (viewModel.routeSelectedForReservation.value?.personnelCount!! < viewModel.routeSelectedForReservation.value?.vehicleCapacity!!){

                viewModel.selectedStation?.let { stop ->

                    val text = if (getString(R.string.generic_language) == "en")
                        getString(R.string.shuttle_make_reservation_info) + fromHtml("<b>" + viewModel.routeTitle.value + "</b>")
                    else
                        fromHtml("<b>" + viewModel.routeTitle.value + "</b>").toString() +  getString(R.string.shuttle_make_reservation_info)

                    FlexigoInfoDialog.Builder(requireContext())
                        .setTitle(getString(R.string.reservation))
                        .setText1(text)
                        .setCancelable(false)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.confirm)) { dialog ->
                            dialog.dismiss()

                            val requestModel = viewModel.getReservationRequestModel(stop)
                            requestModel?.let { model ->
                                viewModel.makeShuttleReservation(model)
                            }
                        }
                        .setCancelButton(getString(R.string.cancel_2)) { dialog ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()

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

        selectDateVisibility()

        binding.layoutRouteNameTimeBack.setOnClickListener {
            if (it != null)
                selectDateVisibility()
        }

        viewModel.successReservation.observe(viewLifecycleOwner) { it ->
            if (it != null && it == true) {

                setDepartureTime(isRoundTrip)

                val days = viewModel.daysLocalValuesMap.value?.toList()?.sortedBy {
                    it.first
                }?.toMap()?.values.toString()


                val dateString = if (viewModel.selectedStartDay.value == viewModel.selectedFinishDay.value){
                    if (getString(R.string.generic_language) == "tr"){
                        viewModel.selectedStartDay.value.toString().plus(" ")
                    } else
                        viewModel.selectedStartDayCalendar.value?.getCustomDateStringEN(withYear = true, withComma = true).plus(" ")

                }
                else{
                    if (getString(R.string.generic_language) == "tr")
                        viewModel.selectedStartDay.value?.plus(" - ").plus(viewModel.selectedFinishDay.value).plus(" ").plus(getString(R.string.between_date)).plus(",")
                    else
                        viewModel.selectedStartDayCalendar.value?.getCustomDateStringEN(withYear = false, withComma = false).plus(getString(R.string.to).lowercase()).plus(" ").plus(viewModel.selectedFinishDayCalendar.value?.getCustomDateStringEN(withYear = true, withComma = true))

                }


                val weekdays = if (isAllWeekdays() && viewModel.daysValues.value?.size() == 5)
                    if (getString(R.string.generic_language) == "tr")
                        getString(R.string.all_weekdays)
                    else
                        getString(R.string.all_weekdays).plus(" ").plus(getString(R.string.from).lowercase())
                else if (viewModel.daysLocalValuesMap.value?.toList()?.size!! > 1)
                    days.replace("[","").replace("]","").plus(getString(R.string.in_days))
                else if (viewModel.daysLocalValuesMap.value?.toList()?.size!! == 1)
                    days.replace("[","").replace("]","").plus(getString(R.string.in_day))
                else
                    ""


                val text = getString(R.string.shuttle_make_reservation_multiple_date_info_text,
                    dateString,
                    weekdays,
                    viewModel.routeName.value,
                    viewModel.departureArrivalTimeTextPopup.value,
                    viewModel.routeTitle.value.plus(" "))

                ReservationDialog.Builder(requireContext())
                    .setTitle(getString(R.string.reservation_confirmation))
                    .setText1(text)
                    .setText2(getString(R.string.reservation_warning))
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                        dialog.dismiss()
                        activity?.finish()

                    }
                    .setCancelButton(getString(R.string.view_reservation)) { dialog ->
                        dialog.dismiss()
                        NavHostFragment.findNavController(this).navigate(R.id.action_routeSearchReservation_to_reservationViewFragment)
                    }
                    .create()
                    .show()

            }
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
                selectDateVisibility()
        }

        viewModel.selectedStartDay.observe(viewLifecycleOwner){
            if (it != null)
                selectDateVisibility()
        }

        binding.textviewDepartureTimeValue.text = viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext())
        binding.textviewDepartureTime.text = viewModel.pickerTitle

        binding.checkboxRoundTrip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                isRoundTrip = isChecked
                setDepartureTime(isChecked)
            } else{
                isRoundTrip = false
                setDepartureTime(isChecked)
                binding.textviewDepartureTimeValue.text = viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext())
                binding.textviewDepartureTime.text = viewModel.pickerTitle
            }
        }

    }

    private fun showAllMarkers(isPaddingTop: Boolean) {
        val builder = LatLngBounds.Builder()
        for (m in markerList)
            builder.include(m.position)

        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.30).toInt()

        if (!isPaddingTop)
            googleMap!!.setPadding(0, 0, 0, binding.bottomSheet.measuredHeight)
        else
            googleMap!!.setPadding(0,  bottomSheetBehavior.peekHeight, 0, 0)

        // Zoom and animate the google map to show all markers
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        googleMap!!.animateCamera(cu)

    }


    private fun isAllWeekdays() : Boolean{
     var isContain = false

        for (i in 1 until 6){
            if (viewModel.daysLocalValuesMap.value?.containsKey(i) == true)
                isContain = true
            else
                return false
        }

        return isContain
    }

    private var isRoundTrip: Boolean = false

    private fun setDepartureTime(isRoundTrip: Boolean){

        var tempFirstString: String = ""
        var tempSecondString: String = ""

        val tempTime = if (viewModel.isFromChanged.value == true){
            viewModel.currentWorkgroupResponse.value?.template?.shift?.returnDepartureHour?.convertHourMinutes(requireContext())
        } else{
            (viewModel.currentWorkgroupResponse.value?.template?.shift?.departureHour ?: viewModel.currentWorkgroupResponse.value?.template?.shift?.arrivalHour).convertHourMinutes(requireContext())
        }

        if (viewModel.isFromChanged.value == true){

            if (isRoundTrip)
                binding.textviewDepartureTime.text = getString(R.string.arrival_departure_time)
            else
                binding.textviewDepartureTime.text = viewModel.pickerTitle

            viewModel.textviewDepartureTime.value = binding.textviewDepartureTime.text as String?
            tempFirstString = getString(R.string.arriving_at)
            tempSecondString = getString(R.string.departing_at)

        } else
        {
            if (isRoundTrip)
                binding.textviewDepartureTime.text = getString(R.string.departure_arrival_time)
            else
                binding.textviewDepartureTime.text = viewModel.pickerTitle

            viewModel.textviewDepartureTime.value = binding.textviewDepartureTime.text as String?
            tempFirstString = getString(R.string.departing_at)
            tempSecondString = getString(R.string.arriving_at)

        }

        if(!isRoundTrip){
            viewModel.departureArrivalTimeText.value = viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext())

            binding.textviewRouteNameAndTime.text = viewModel.routeName.value.plus(", ")
                .plus(tempFirstString).plus(" ").plus(viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext()))

            if (getString(R.string.generic_language) == "tr"){

                viewModel.departureArrivalTimeTextPopup.value = viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext()).plus(" ").plus(tempFirstString)
            } else
            {
                viewModel.departureArrivalTimeTextPopup.value = tempFirstString.plus(" ").plus(viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext()))
            }

        } else{
            viewModel.departureArrivalTimeText.value = viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext()).plus(" - ").plus(tempTime)

            binding.textviewRouteNameAndTime.text = viewModel.routeName.value.plus(", ")
                .plus(tempFirstString).plus(" ").plus(viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext()))
                .plus(" - ").plus(tempSecondString).plus(" ").plus(tempTime)

            if (getString(R.string.generic_language) == "tr"){
                viewModel.departureArrivalTimeTextPopup.value = viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext()).plus(" ").plus(tempFirstString).plus(" , ")
                    .plus(tempTime).plus(" ").plus(tempSecondString)
            } else
            {
                viewModel.departureArrivalTimeTextPopup.value = tempFirstString.plus(" ").plus(viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext())).plus(" ").plus(getString(R.string.and))
                    .plus(" ").plus(tempSecondString).plus(" ").plus(tempTime)
            }

        }

    }

    private fun selectDateVisibility(){

        if (viewModel.selectedStartDay.value != viewModel.selectedFinishDay.value){
            binding.layoutSelectDate.visibility = View.VISIBLE
            checkBoxSelectableControl()

        } else {
            binding.layoutSelectDate.visibility = View.GONE
            val jsonArrayLocalMap = LinkedHashMap<Int, String>()

            val today = longToCalendar(viewModel.selectedStartDayCalendar.value?.time)
            jsonArrayLocalMap[0] = today?.time.convertForWeekDaysLocal()

            val jsonArray = JsonArray()
            jsonArray.add(today?.time.convertForWeekDaysLiteral().uppercase())
            viewModel.daysValues.value = jsonArray

            viewModel.daysLocalValuesMap.value = jsonArrayLocalMap

        }

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
                    val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat,
                        maxLng
                    )), (resources.displayMetrics.widthPixels * 0.30).toInt())
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

    private val markerList : MutableList<Marker> = ArrayList()

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

        getDestinationInfo()

        val minuteText = requireContext().getString(R.string.short_minute)
        val walkingDurationInMin = route.closestStation?.durationInMin?.toInt() ?: 0
        val walkingDurationInMinDisplayString = walkingDurationInMin.toString().plus(minuteText)

        binding.textViewDurationWalking.text = walkingDurationInMinDisplayString
        binding.textviewDurationTrip.text = String.format("%.1f", route.durationInMin ?: 0.0).plus(minuteText)
        viewModel.routeTitle.value = route.title
        viewModel.routeName.value = route.destination.name
        binding.textviewTotalValue.text = "  ".plus("${(walkingDurationInMin) + (route.durationInMin?.toInt() ?: 0)}${minuteText}")

        if(route.vehicle.plateId == "" || route.vehicle.plateId == null) {
            binding.textviewPlateValue.text = getString(R.string.not_assigned)
            binding.textviewPlateValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
        }
        else{
            binding.textviewPlateValue.text = route.vehicle.plateId
            binding.textviewPlateValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkNavyBlue))
        }

        viewModel.departureArrivalTimeText.value  = viewModel.selectedDate?.date.convertToShuttleDateTime(requireContext())

        if (viewModel.currentWorkgroupResponse.value?.template?.direction == WorkgroupDirection.ROUND_TRIP)
            binding.checkboxRoundTrip.visibility = View.VISIBLE
        else
            binding.checkboxRoundTrip.visibility = View.GONE


        val startDateFormat = if (getString(R.string.generic_language) == "tr"){
            viewModel.selectedStartDayCalendar.value?.convertToShuttleDate()
        } else {
            viewModel.selectedStartDayCalendar.value?.getCustomDateStringEN().toString()
        }

        binding.textviewStartValue.text = startDateFormat

        val finishDateFormat = if (getString(R.string.generic_language) == "tr"){
            viewModel.selectedFinishDayCalendar.value?.convertToShuttleDate()
        } else {
            viewModel.selectedFinishDayCalendar.value?.getCustomDateStringEN().toString()
        }

        binding.textviewFinishValue.text = finishDateFormat


        fillDestination()
        showAllMarkers(false)
    }

    private fun checkBoxSelectableControl(){

        val afterSevenDays = longToCalendar(viewModel.selectedStartDayCalendar.value?.time)
        afterSevenDays?.add(Calendar.DATE, 7)

        val isShorterThanSevenDays = afterSevenDays?.time!!.time >= viewModel.selectedFinishDayCalendar.value?.time!!

        val jsonArray = JsonArray()
        val jsonArrayLocalMap = LinkedHashMap<Int, String>()

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
                            jsonArrayLocalMap[day.getSortId()] = day.getLocalLong(requireContext())
                        }

                        viewModel.daysValues.value = jsonArray
                        viewModel.daysLocalValuesMap.value = jsonArrayLocalMap

                        checkbox.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked){
                                if (!jsonArray.toString().contains(day.name.uppercase())) {
                                    jsonArray.add(day.name.uppercase())
                                    jsonArrayLocalMap[day.getSortId()] = day.getLocalLong(requireContext())
                                }

                                viewModel.daysValues.value = jsonArray
                                viewModel.daysLocalValuesMap.value = jsonArrayLocalMap

                            } else{
                                for (i in 0 until jsonArray.size() - 1){
                                    if (jsonArray.get(i).asString == day.name.uppercase()) {
                                        jsonArray.remove(i)
                                        jsonArrayLocalMap.values.removeAll(Collections.singleton(day.getLocalLong(requireContext())))

                                    }
                                }

                                viewModel.daysValues.value = jsonArray
                                viewModel.daysLocalValuesMap.value = jsonArrayLocalMap

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

    enum class DAYS(private val labelId: Int, private val labelIdLong: Int, private val sortId: Int) {
        MONDAY(R.string.material_calendar_monday, R.string.monday, 1),
        TUESDAY(R.string.material_calendar_tuesday, R.string.tuesday, 2),
        WEDNESDAY(R.string.material_calendar_wednesday, R.string.wednesday, 3),
        THURSDAY(R.string.material_calendar_thursday, R.string.thursday, 4),
        FRIDAY(R.string.material_calendar_friday, R.string.friday, 5),
        SATURDAY(R.string.material_calendar_saturday, R.string.saturday, 6),
        SUNDAY(R.string.material_calendar_sunday, R.string.sunday, 7);

        fun getLocal(context: Context) = context.getString(labelId)
        fun getLocalLong(context: Context) = context.getString(labelIdLong)
        fun getSortId() = sortId
    }

    private fun fillDestination() {
        val marker: Marker?

        val homeLocation = AppDataManager.instance.personnelInfo?.homeLocation

        if (viewModel.isLocationToHome.value == true) {
            marker = googleMap?.addMarker(MarkerOptions().position(LatLng(homeLocation!!.latitude, homeLocation.longitude)).icon(homeIcon))
            marker?.tag = viewModel.toLabelText.value
        }
        else {
            marker = googleMap?.addMarker(MarkerOptions().position(LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude)).icon(toLocationIcon))
            marker?.tag = viewModel.toLabelText.value
        }

        if (marker != null) {
            markerList.add(marker)
        }

        val markerDest = googleMap?.addMarker(MarkerOptions().position(destinationLatLng ?: LatLng(0.0, 0.0)).icon(workplaceIcon))
        markerDest?.tag = viewModel.fromLabelText.value

        if (markerDest != null) {
            markerList.add(markerDest)
        }

        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude), 12f))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng ?: LatLng(0.0, 0.0), 12f))

    }

    private fun getDestinationInfo(){

        viewModel.destinations.value?.let { destinations ->
            destinations.forEachIndexed { _, destinationModel ->

                if (viewModel.currentWorkgroup.value != null && (viewModel.currentWorkgroup.value?.fromType == FromToType.CAMPUS || viewModel.currentWorkgroup.value?.fromType == FromToType.PERSONNEL_WORK_LOCATION)){
                    if(destinationModel.id == viewModel.currentWorkgroup.value!!.fromTerminalReferenceId) {
                        destinationLatLng = LatLng(destinationModel.location!!.latitude, destinationModel.location.longitude)
                    }
                } else{
                    if(viewModel.currentWorkgroup.value != null && destinationModel.id == viewModel.currentWorkgroup.value!!.toTerminalReferenceId) {
                        destinationLatLng = LatLng(destinationModel.location!!.latitude, destinationModel.location.longitude)
                    }
                }

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

    override fun getViewModel(): RouteSearchViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[RouteSearchViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "RouteSearchReservationFragment"
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