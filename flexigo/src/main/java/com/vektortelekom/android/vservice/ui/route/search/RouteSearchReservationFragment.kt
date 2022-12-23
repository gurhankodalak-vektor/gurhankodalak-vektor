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

        binding.mapView.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels - 200f.dpToPx(requireContext())

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

            viewModel.selectedStation?.let { stop ->
                val requestModel = viewModel.getReservationRequestModel(stop)
                requestModel?.let { model ->
                    viewModel.makeShuttleReservation(model)
                }

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
                    if (resources.configuration.locale.language.equals("tr")){
                        viewModel.selectedStartDay.value.toString().plus(" ")
                    } else
                    {
                        viewModel.selectedStartDayCalendar.value?.getCustomDateStringEN(withYear = true, withComma = true).plus(" ")
                    }
                }
                else{
                    if (resources.configuration.locale.language.equals("tr"))
                        viewModel.selectedStartDay.value?.plus(" - ").plus(viewModel.selectedFinishDay.value).plus(" ").plus(getString(R.string.between_date)).plus(",")
                    else
                        viewModel.selectedStartDayCalendar.value?.getCustomDateStringEN(withYear = false, withComma = false).plus(getString(R.string.to).lowercase()).plus(" ").plus(viewModel.selectedFinishDayCalendar.value?.getCustomDateStringEN(withYear = true, withComma = true))

                }


                val weekdays = if (isAllWeekdays() && viewModel.daysValues.value?.size() == 5)
                    if (resources.configuration.locale.language.equals("tr"))
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

    private fun returnTripReservation() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setCancelable(false)
        dialog.setTitle(resources.getString(R.string.return_trip))
        dialog.setMessage(resources.getString(R.string.return_trip_message))
        dialog.setPositiveButton(resources.getString(R.string.make_return_reservation)) { d, _ ->
            d.dismiss()
        }
        dialog.setNegativeButton(resources.getString(R.string.no_thanks)) { d, _ ->
            d.dismiss()
        }
        dialog.show()
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
            viewModel.currentWorkgroupResponse.value?.template?.shift?.returnDepartureHour?.convertHourMinutes()
        } else{
            (viewModel.currentWorkgroupResponse.value?.template?.shift?.departureHour ?: viewModel.currentWorkgroupResponse.value?.template?.shift?.arrivalHour).convertHourMinutes()
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
            viewModel.departureArrivalTimeText.value = viewModel.selectedDate?.date.convertToShuttleDateTime()

            binding.textviewRouteNameAndTime.text = viewModel.routeName.value.plus(", ")
                .plus(tempFirstString).plus(" ").plus(viewModel.selectedDate?.date.convertToShuttleDateTime())

            if (resources.configuration.locale.language.equals("tr")){

                viewModel.departureArrivalTimeTextPopup.value = viewModel.selectedDate?.date.convertToShuttleDateTime().plus(" ").plus(tempFirstString)
            } else
            {
                viewModel.departureArrivalTimeTextPopup.value = tempFirstString.plus(" ").plus(viewModel.selectedDate?.date.convertToShuttleDateTime())
            }

        } else{
            viewModel.departureArrivalTimeText.value = viewModel.selectedDate?.date.convertToShuttleDateTime().plus(" - ").plus(tempTime)

            binding.textviewRouteNameAndTime.text = viewModel.routeName.value.plus(", ")
                .plus(tempFirstString).plus(" ").plus(viewModel.selectedDate?.date.convertToShuttleDateTime())
                .plus(" - ").plus(tempSecondString).plus(" ").plus(tempTime)

            if (resources.configuration.locale.language.equals("tr")){
                viewModel.departureArrivalTimeTextPopup.value = viewModel.selectedDate?.date.convertToShuttleDateTime().plus(" ").plus(tempFirstString).plus(" , ")
                    .plus(tempTime).plus(" ").plus(tempSecondString)
            } else
            {
                viewModel.departureArrivalTimeTextPopup.value = tempFirstString.plus(" ").plus(viewModel.selectedDate?.date.convertToShuttleDateTime()).plus(" ").plus(getString(R.string.and))
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
                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(station.location.latitude, station.location.longitude), 10f)
                googleMap?.moveCamera(cu)
                googleMap?.animateCamera(cu)
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
        binding.textviewDurationTrip.text = String.format("%.1f", route.durationInMin).plus(minuteText)
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

        viewModel.departureArrivalTimeText.value  = viewModel.selectedDate?.date.convertToShuttleDateTime()

        if (viewModel.currentWorkgroupResponse.value?.template?.direction == WorkgroupDirection.ROUND_TRIP)
            binding.checkboxRoundTrip.visibility = View.VISIBLE
        else
            binding.checkboxRoundTrip.visibility = View.GONE


        val startDateFormat = if (resources.configuration.locale.language.equals("tr")){
            viewModel.selectedStartDayCalendar.value?.convertToShuttleDate()
        } else {
            viewModel.selectedStartDayCalendar.value?.getCustomDateStringEN().toString()
        }

        binding.textviewStartValue.text = startDateFormat

        val finishDateFormat = if (resources.configuration.locale.language.equals("tr")){
            viewModel.selectedFinishDayCalendar.value?.convertToShuttleDate()
        } else {
            viewModel.selectedFinishDayCalendar.value?.getCustomDateStringEN().toString()
        }

        binding.textviewFinishValue.text = finishDateFormat


        fillDestination()
    }

    private fun checkBoxSelectableControl(){
        var isShorterThanSevenDays = true

        val afterSevenDays = longToCalendar(viewModel.selectedStartDayCalendar.value?.time)
        afterSevenDays?.add(Calendar.DATE, 7)

        isShorterThanSevenDays = afterSevenDays?.time!!.time >= viewModel.selectedFinishDayCalendar.value?.time!!

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
        if (viewModel.isLocationToHome.value == true)
            googleMap?.addMarker(MarkerOptions().position(LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude)).icon(homeIcon))?.tag = viewModel.toLabelText.value
        else
            googleMap?.addMarker(MarkerOptions().position(LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude)).icon(toLocationIcon))?.tag = viewModel.toLabelText.value

        googleMap?.addMarker(MarkerOptions().position(destinationLatLng ?: LatLng(0.0, 0.0)).icon(workplaceIcon))?.tag = viewModel.fromLabelText.value

        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude), 10f))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng ?: LatLng(0.0, 0.0), 10f))

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