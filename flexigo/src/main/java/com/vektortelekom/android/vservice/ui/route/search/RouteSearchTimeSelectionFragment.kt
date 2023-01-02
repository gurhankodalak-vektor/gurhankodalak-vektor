package com.vektortelekom.android.vservice.ui.route.search

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isEmpty
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.maps.android.SphericalUtil
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.RouteSearchTimeSelectionFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.route.bottomsheet.BottomSheetSingleDateCalendar
import com.vektortelekom.android.vservice.ui.shuttle.map.ShuttleInfoWindowAdapter
import com.vektortelekom.android.vservice.utils.*
import java.util.*
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class RouteSearchTimeSelectionFragment : BaseFragment<RouteSearchViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RouteSearchViewModel

    lateinit var binding: RouteSearchTimeSelectionFragmentBinding

    private var googleMap: GoogleMap? = null

    private var workplaceIcon: BitmapDescriptor? = null
    private var homeIcon: BitmapDescriptor? = null
    private var toLocationIcon: BitmapDescriptor? = null

    private lateinit var locationClient: FusedLocationClient

    private var toLocationMarker: Marker? = null
    private var directionMarker: Marker? = null

    var destination : DestinationModel? = null

    private var loopFirstElement: Int = 0
    private var loopLastElement: Int = 0

    var hasInitializedRootView = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate<RouteSearchTimeSelectionFragmentBinding>(inflater, R.layout.route_search_time_selection_fragment, container, false).apply {
                lifecycleOwner = this@RouteSearchTimeSelectionFragment
                viewModel = this@RouteSearchTimeSelectionFragment.viewModel
            }
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!hasInitializedRootView){

            hasInitializedRootView = true

            binding.mapView.onCreate(savedInstanceState)

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
                homeIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_home)
                toLocationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_route_to_yellow)


                if (arguments != null && arguments?.getBoolean("isReturnTrip") != null && arguments?.getBoolean("isReturnTrip")!!)
                    replaceFromToTo()

                googleMap?.let { it1 -> drawArcPolyline(it1, LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude), LatLng(viewModel.fromLocation.value!!.latitude, viewModel.fromLocation.value!!.longitude)) }

            }

            binding.mapView.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels - 200f.dpToPx(requireContext())


            setDataForScreen()

        }


        viewModel.demandWorkgroupResponse.observe(viewLifecycleOwner) {

            if (it != null) {
                viewModel.bottomSheetBehaviorEditShuttleState.value  = BottomSheetBehavior.STATE_HIDDEN
                viewModel.demandWorkgroupResponse.value = null

                if (it.error != null) {
                    it.error!!.message?.let { it1 ->
                        activity?.let { it2 ->
                            AppDialog.Builder(it2)
                                .setIconVisibility(false)
                                .setSubtitle(it1)
                                .setOkButton(R.string.got_it_2) { d ->
                                    d.dismiss()
                                }
                                .create().show()
                        }
                    }
                }
                else  {
                    FlexigoInfoDialog.Builder(requireContext())
                        .setTitle(getString(R.string.shuttle_workgroup_success_title))
                        .setText1(getString(R.string.shuttle_workgroup_success_text))
                        .setCancelable(false)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()

                        }
                        .create()
                        .show()

                }
            }

        }

        viewModel.isSelectedTime.observe(viewLifecycleOwner) {
            if (it != null && it == true){

                if (!binding.chipGroup.isEmpty())
                    binding.chipGroup.removeAllViews()

                if (viewModel.selectedDateIndex != 0) {

                    loopFirstElement = viewModel.selectedDateIndex?.minus(1)!!
                    loopLastElement = viewModel.selectedDateIndex?.plus(1)!!
                } else
                {
                    loopFirstElement = viewModel.selectedDateIndex!!
                    loopLastElement = viewModel.selectedDateIndex?.plus(2)!!
                }

                for (i in loopFirstElement.until(loopLastElement)) {
                    val chip = layoutInflater.inflate(R.layout.chip_small, requireView().parent.parent as ViewGroup, false) as Chip
                    chip.text = viewModel.dateAndWorkgroupList?.get(i)?.date.convertToShuttleDateTime()
                    if (i == viewModel.selectedDateIndex)
                        chip.isChecked = true
                    chip.id = View.generateViewId()
                    chip.isClickable = true
                    chip.isCheckable = true
                    chip.isChipIconVisible = false
                    chip.isCheckedIconVisible = false
                    chip.tag = viewModel.dateAndWorkgroupList?.get(i)?.date

                    binding.chipGroup.addView(chip)


                }

            }
        }

        viewModel.selectedCalendarDay.observe(viewLifecycleOwner) {
            setDatesForEditShuttle(
                destinationId = viewModel.destinationId!!,
                isFirstOpen = false,
                date = it
            )

        }

        viewModel.haveSearchedRoutes.observe(viewLifecycleOwner){
            if (it != null && it){
                if (viewModel.searchedRoutes.value != null && viewModel.searchedRoutes.value!!.isNotEmpty())
                    NavHostFragment.findNavController(this).navigate(R.id.action_routeSearchTimeSelectionFragment_to_routesSearchResultFragment)
                else{
                    FlexigoInfoDialog.Builder(requireContext())
                        .setTitle(getString(R.string.no_route_your_request))
                        .setCancelable(false)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
                viewModel.haveSearchedRoutes.value = null
            }

        }

        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)
            chip?.let {chipView ->

                viewModel.dateAndWorkgroupList!!.forEachIndexed { index, dateWithWorkgroup ->
                    if (chipView.isChecked && chipView.tag == dateWithWorkgroup.date) {
                        viewModel.selectedDate = dateWithWorkgroup
                        viewModel.selectedDateIndex = index
                        viewModel.isSelectedTime.value = true

                        viewModel.selectedShiftIndex = dateWithWorkgroup.workgroupIndex!!
                        viewModel.currentWorkgroup.value = viewModel.campusFilter.value?.get(viewModel.selectedShiftIndex)

                        viewModel.getWorkgroupInformation(viewModel.selectedDate!!.workgroupId)
                    }
                }

            } ?: kotlin.run {
            }

        }

        binding.imageviewBack.setOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        binding.imagebuttonRouteInfoEdit.setOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        binding.textviewAll.setOnClickListener {
            viewModel.openNumberPicker.value = RouteSearchViewModel.SelectType.Time
        }

        binding.layoutDateEdit.setOnClickListener {
            val bottomSheetSingleDateCalendar = BottomSheetSingleDateCalendar()
            bottomSheetSingleDateCalendar.show(requireActivity().supportFragmentManager, bottomSheetSingleDateCalendar.tag)
        }

        binding.buttonContinue.setOnClickListener {
            viewModel.selectedDate?.let { selectedDate ->

                when(selectedDate.workgroupStatus) {
                    WorkgroupStatus.PENDING_DEMAND -> {

                        FlexigoInfoDialog.Builder(requireContext())
                            .setTitle(getString(R.string.shuttle_request))
                            .setText1(String.format(getString(R.string.shuttle_demand_info), viewModel.fromLabelText.value, selectedDate.date.convertToShuttleDateTime()))
                            .setCancelable(false)
                            .setIconVisibility(false)
                            .setOkButton(getString(R.string.shuttle_send_demand)) { dialog ->
                                dialog.dismiss()

                                viewModel.demandWorkgroup(WorkgroupDemandRequest(
                                    workgroupInstanceId = selectedDate.workgroupId,
                                    stationId = viewModel.selectedFromDestination?.id,
                                    location =  LocationModel2(latitude = viewModel.selectedToLocation?.location?.latitude, longitude = viewModel.selectedToLocation?.location?.longitude)

                                ))

                            }
                            .setCancelButton(getString(R.string.Generic_Close)) { dialog ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                    WorkgroupStatus.PENDING_PLANNING -> {

                        FlexigoInfoDialog.Builder(requireContext())
                            .setText1(getString(R.string.opt_time_over))
                            .setCancelable(false)
                            .setIconVisibility(false)
                            .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                                dialog.dismiss()
                            }
                            .setCancelButton(getString(R.string.Generic_Close)) { dialog ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                    else -> {
                        val from =
                            SearchRequestModel(
                                lat = viewModel.fromLocation.value?.latitude,
                                lng = viewModel.fromLocation.value?.longitude,
                                destinationId = null
                            )

                        val to =
                            SearchRequestModel(
                                lat = viewModel.selectedToLocation?.location?.latitude,
                                lng = viewModel.selectedToLocation?.location?.longitude,
                                destinationId = null
                            )

                        viewModel.getStops(
                            RouteStopRequest(
                                from = from,
                                whereto = to,
                                shiftId = null,
                                workgroupInstanceId = selectedDate.workgroupId
                            ),
                            requireContext()
                        )

                    }
                }

            }

        }
    }

    private fun setDataForScreen(){
        binding.textviewFromName.text = viewModel.fromLabelText.value.plus(" - ")
        binding.textviewToName.text = viewModel.toLabelText.value

        if (viewModel.currentWorkgroup.value != null && viewModel.currentWorkgroup.value?.firstDepartureDate != null){

            viewModel.currentWorkgroup.value?.firstDepartureDate?.getDateWithZeroHour()
                ?.let {
                    setDatesForEditShuttle(
                        destinationId = viewModel.destinationId!!,
                        isFirstOpen = true,
                        date = it
                    )
                }
        } else{

            setDatesForEditShuttle(
                destinationId = viewModel.destinationId!!,
                isFirstOpen = true,
                date = Calendar.getInstance().time.time
            )
        }

    }

    private fun replaceFromToTo(){

        val tempToLabel = viewModel.toLabelText.value
        val tempFromLabel = viewModel.fromLabelText.value

        val tempToLocation = viewModel.toLocation.value
        val tempFromLocation = viewModel.fromLocation.value

        viewModel.toLocation.value = tempFromLocation
        viewModel.fromLocation.value = tempToLocation

        viewModel.toLabelText.value = tempFromLabel
        viewModel.fromLabelText.value = tempToLabel

        val tempToIcon = viewModel.toIcon.value
        val tempFromIcon = viewModel.fromIcon.value

        viewModel.toIcon.value = tempFromIcon
        viewModel.fromIcon.value = tempToIcon

        val tempIsFromChanged = viewModel.isFromChanged.value
        viewModel.isFromChanged.value = tempIsFromChanged != true

        val tempRotation = directionMarker?.rotation
        directionMarker?.rotation = tempRotation?.plus(180)!!

        setDataForScreen()
    }

    private fun setDatesForEditShuttle(
        destinationId: Long,
        isFirstOpen: Boolean,
        date: Long
    ) {

        viewModel.allWorkgroup.value?.let { workgroup ->

            val nextDay = date + 1000L * 60 * 60 * 24

            viewModel.selectedDate = null
            viewModel.selectedDateIndex = null
            viewModel.dateAndWorkgroupList = null

            val dateAndWorkgroupMap = mutableMapOf<Int, RouteSearchViewModel.DateAndWorkgroup>()
            var i = 0

            viewModel.campusFilter.value = workgroup.filter { workgroup ->
                if (viewModel.isFromChanged.value == false)
                    destinationId == workgroup.fromTerminalReferenceId
                else
                    destinationId == workgroup.toTerminalReferenceId
            }.filter { ride ->  ride.firstDepartureDate in date until nextDay }


            viewModel.campusFilter.value!!.map {
                dateAndWorkgroupMap[i] =
                    RouteSearchViewModel.DateAndWorkgroup(
                        it.firstDepartureDate,
                        it.workgroupInstanceId,
                        it.workgroupStatus,
                        it.fromType,
                        it.fromTerminalReferenceId,
                        it,
                        null,
                        i
                    )
                i++
            }

            viewModel.dateAndWorkgroupList = dateAndWorkgroupMap.values.toList()

            if (viewModel.dateAndWorkgroupList!!.isEmpty()){
                binding.textviewWarning.visibility = View.VISIBLE
                binding.buttonContinue.isEnabled = false
            } else {
                binding.textviewWarning.visibility = View.GONE
                binding.buttonContinue.isEnabled = true
            }

            if (isFirstOpen) {

                viewModel.dateAndWorkgroupList!!.forEachIndexed { index, dateWithWorkgroup ->
                    if (viewModel.currentWorkgroup.value?.firstDepartureDate == dateWithWorkgroup.date
                        && viewModel.currentWorkgroup.value?.workgroupInstanceId == dateWithWorkgroup.workgroupId) {
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

            val startDate = longToCalendar(viewModel.currentWorkgroupResponse.value?.instance?.startDate) ?: Calendar.getInstance()

            val date1: Date? =  if (viewModel.currentWorkgroupResponse.value != null){
                longToCalendar(viewModel.currentWorkgroupResponse.value!!.instance.startDate!!)?.time.convertForTimeCompare()
            } else{
                longToCalendar(Calendar.getInstance().time.time)?.time.convertForTimeCompare()
            }

            val date2: Date? =  if (viewModel.selectedDate != null){
                longToCalendar(viewModel.selectedDate?.date)?.time.convertForTimeCompare()
            } else{
                if (viewModel.selectedCalendarDay.value != null)
                    longToCalendar(viewModel.selectedCalendarDay.value)?.time.convertForTimeCompare()
                else
                    longToCalendar(Calendar.getInstance().time.time)?.time.convertForTimeCompare()
            }


            if (date1?.compareTo(date2)!! > 0 || date1.compareTo(date2) == 0){
                viewModel.selectedStartDay.value = startDate.time.convertToShuttleDate()
                viewModel.selectedStartDayCalendar.value = startDate.time
                viewModel.selectedFinishDayCalendar.value = startDate.time
            } else {
                viewModel.selectedStartDay.value = date2?.convertToShuttleDate()
                viewModel.selectedStartDayCalendar.value = date2
                viewModel.selectedFinishDayCalendar.value = date2
            }

            viewModel.dateValueText.value = viewModel.startDateFormatted(getString(R.string.generic_language))
            binding.textviewDateValue.text = viewModel.startDateFormatted(getString(R.string.generic_language))

            viewModel.selectedFinishDay.value = viewModel.selectedStartDay.value

            if (viewModel.dateAndWorkgroupList != null && viewModel.dateAndWorkgroupList!!.size > 3)
                binding.textviewAll.visibility = View.VISIBLE
            else
                binding.textviewAll.visibility = View.GONE

            if (!binding.chipGroup.isEmpty())
                binding.chipGroup.removeAllViews()

            addChipToGroup(binding.chipGroup)
        }

        if (viewModel.isFromChanged.value == true){
            viewModel.pickerTitle = getString(R.string.arrival_time_2)
            binding.textviewDepartureTime.text = getString(R.string.arrival_time_2)
        } else{
            viewModel.pickerTitle = getString(R.string.departure_time_4)
            binding.textviewDepartureTime.text = getString(R.string.departure_time_4)
        }

    }


    private fun addChipToGroup(group: ChipGroup){
        var maxCount = 0

        if (viewModel.dateAndWorkgroupList!!.size > 3){

            for (list in viewModel.dateAndWorkgroupList!!){
                if (maxCount <= 1)
                {
                    val chip = layoutInflater.inflate(R.layout.chip_small, requireView().parent.parent as ViewGroup, false) as Chip
                    chip.text = list.date.convertToShuttleDateTime()
                    if (maxCount == 0) {
                        chip.isChecked = true
                        viewModel.selectedDate = list
                        viewModel.selectedDateIndex = maxCount
                    }
                    chip.id = View.generateViewId()
                    chip.isClickable = true
                    chip.isCheckable = true
                    chip.isChipIconVisible = false
                    chip.isCheckedIconVisible = false
                    chip.tag = list.date

                    maxCount ++

                    group.addView(chip)
                }

            }
        } else{

            for (list in viewModel.dateAndWorkgroupList!!){

                    val chip = layoutInflater.inflate(R.layout.chip_small, requireView().parent.parent as ViewGroup, false) as Chip
                    chip.text = list.date.convertToShuttleDateTime()
                    if (maxCount == 0) {
                        chip.isChecked = true
                        viewModel.selectedDate = list
                        viewModel.selectedDateIndex = maxCount
                    }
                    chip.id = View.generateViewId()
                    chip.isClickable = true
                    chip.isCheckable = true
                    chip.isChipIconVisible = false
                    chip.isCheckedIconVisible = false
                    chip.tag = list.date

                    group.addView(chip)


            }
        }

    }

    private fun drawArcPolyline(googleMap: GoogleMap, latLng1: LatLng, latLng2: LatLng) {
        toLocationMarker?.remove()

        toLocationMarker = if (viewModel.isLocationToHome.value == true)
            googleMap.addMarker(MarkerOptions().position(latLng1).icon(homeIcon))
        else
            googleMap.addMarker(MarkerOptions().position(latLng1).icon(toLocationIcon))

        val destinationMarker = googleMap.addMarker(MarkerOptions().position(latLng2).icon(workplaceIcon))

        if (destinationMarker != null) {
            destinationMarker.tag = destination
        }
        if (toLocationMarker != null) {
            toLocationMarker!!.tag = viewModel.selectedToLocation!!.text
        }

        val builder = LatLngBounds.Builder()

        builder.include(toLocationMarker!!.position)
        builder.include(destinationMarker!!.position)

        val bounds = builder.build()

        try {
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, 150)
            googleMap.moveCamera(cu)
            googleMap.animateCamera(cu)
        }
        catch (e: Exception) {

        }

        val k = 0.5 //curve radius
        var h = SphericalUtil.computeHeading(latLng1, latLng2)
        var d = 0.0
        val p: LatLng?

        if (h < 0) {
            d = SphericalUtil.computeDistanceBetween(latLng2, latLng1)
            h = SphericalUtil.computeHeading(latLng2, latLng1)
            //Midpoint position
            p = SphericalUtil.computeOffset(latLng2, d * 0.5, h)
        } else {
            d = SphericalUtil.computeDistanceBetween(latLng1, latLng2)
            p = SphericalUtil.computeOffset(latLng1, d * 0.5, h)
        }

        //Apply some mathematics to calculate position of the circle center
        val x = (1 - k * k) * d * 0.5 / (2 * k)
        val r = (1 + k * k) * d * 0.5 / (2 * k)

        val c = SphericalUtil.computeOffset(p, x, h + 90.0)

        //Calculate heading between circle center and two points
        val h1 = SphericalUtil.computeHeading(c, latLng1)
        val h2 = SphericalUtil.computeHeading(c, latLng2)

        //Calculate positions of points on circle border and add them to polyline options
        val numberOfPoints = 1000
        val step = (h2 - h1) / numberOfPoints

        val polygon = PolygonOptions()
        val temp = arrayListOf<LatLng>()

        for (i in 0 until numberOfPoints) {
            val latlng = SphericalUtil.computeOffset(c, r, h1 + i * step)
            polygon.add(latlng)
            temp.add(latlng)
        }

        for (i in (temp.size - 1) downTo 1) {
            polygon.add(temp[i])
        }

        polygon.strokeColor(Color.BLACK)
        polygon.strokeWidth(10f)
        googleMap.addPolygon(polygon)

        directionMarker = if(viewModel.isFromChanged.value == false){
            googleMap.addMarker(MarkerOptions().position(temp[numberOfPoints/2]).anchor(0.5f,0.5f)
                .rotation((90 - bearingBetweenLocations(latLng2, latLng1)).toFloat())
                .flat(true)
                .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_back)))

        } else{
            googleMap.addMarker(MarkerOptions().position(temp[numberOfPoints/2]).anchor(0.5f,0.5f)
                .rotation((90 - bearingBetweenLocations(latLng2, latLng1)).toFloat())
                .flat(true)
                .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_back)))
        }


        temp.clear()
    }

    private fun bearingBetweenLocations(latLng1: LatLng, latLng2: LatLng): Double {
        val lat1 = latLng1.latitude
        val long1 = latLng1.longitude
        val lat2 = latLng2.latitude
        val long2 = latLng2.longitude
        val dLon = long2 - long1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - (sin(lat1) * cos(lat2) * cos(dLon))
        var brng = atan2(y, x)
        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360

        return brng
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
        const val TAG: String = "RouteSearchTimeSelectionFragment"
        fun newInstance() = RouteSearchTimeSelectionFragment()

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