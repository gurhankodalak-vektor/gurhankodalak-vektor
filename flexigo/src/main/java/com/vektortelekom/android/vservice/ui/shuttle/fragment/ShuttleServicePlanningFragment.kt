package com.vektortelekom.android.vservice.ui.shuttle.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.applandeo.materialcalendarview.EventDay
import com.google.android.gms.location.LocationRequest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupInstance
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupTemplate
import com.vektortelekom.android.vservice.databinding.ShuttleServicePlanningBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.calendar.dialog.CalendarSendDemandWorkgroupDialog
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.shuttle.adapter.*
import com.vektortelekom.android.vservice.utils.*
import java.util.*
import javax.inject.Inject

class ShuttleServicePlanningFragment : BaseFragment<ShuttleViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: ShuttleServicePlanningBinding

    private lateinit var calendarToday: Calendar

    private var shuttleReservationAdapter: ShuttleReservationAdapter? = null
    private var shuttleWorkgroupInstanceAdapter: ShuttleWorkgroupInstanceAdapter? = null
    private var shuttleDemandAdapter: ShuttleDemandAdapter? = null
    private var shuttleRegularRoutesAdapter: ShuttleRegularRoutesAdapter? = null

    private var dayModelMap: MutableMap<Long, ShuttleDayModelFromNextRides> = mutableMapOf()

    private var dayModelMapAllRides: MutableMap<Long, ShuttleDayModelFromNextRides> = mutableMapOf()

    var tempDayModelWorkGroupInstancesFiltered = mutableListOf<WorkGroupInstance>()

    private var dayModelWorkgroupsInstances: MutableList<WorkGroupInstance>? = mutableListOf()
    private var dayModelWorkgroupsTemplates: MutableList<WorkGroupTemplate>? = mutableListOf()

    var fromLocation: SearchRequestModel? = null
    var toLocation: SearchRequestModel? = null

    private var currentRoute: RouteModel? = null

    private lateinit var placesClient: PlacesClient


    @Volatile
    private var myLocation: Location? = null
    private lateinit var locationClient: FusedLocationClient

    private var selectedDay: EventDay? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<ShuttleServicePlanningBinding>(inflater, R.layout.shuttle_service_planning, container, false).apply {
            lifecycleOwner = this@ShuttleServicePlanningFragment
            viewModel = this@ShuttleServicePlanningFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isLocationToHome.value = true

        calendarToday = Calendar.getInstance()
        workgroupsEmpty()
        if (viewModel.calendarSelectedDay != null) {
            calendarToday.timeInMillis = viewModel.calendarSelectedDay.time
            binding.calendarView.setDate(viewModel.calendarSelectedDay)
        }
        else {
            calendarToday.timeInMillis = Date().getDayWithoutHoursAndMinutesAsLong()
        }
        if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        } else {
            onLocationPermissionFailed()
        }

        // workgroup api
        viewModel.requestWorkGroups()


        viewModel.requestWorkGroupsResponse.observe(viewLifecycleOwner) {

            dayModelWorkgroupsInstances = it.instances
            dayModelWorkgroupsTemplates = it.templates

            setWorkGroups(binding.calendarView.selectedDate)
        }

        placesClient = Places.createClient(requireContext())

        binding.calendarView.setHeaderVisibility(View.GONE)


        viewModel.navigator?.setToolBarText(calendarToday.convertForShuttleDay())

        binding.calendarView.setOnPreviousPageChangeListener {
            calendarToday.add(Calendar.MONTH, -1)
            viewModel.navigator?.setToolBarText(calendarToday.convertForShuttleDay())
        }

        binding.calendarView.setOnForwardPageChangeListener {
            calendarToday.add(Calendar.MONTH, 1)
            viewModel.navigator?.setToolBarText(calendarToday.convertForShuttleDay())
        }

        binding.calendarView.setOnDayClickListener { day ->
            selectedDay = day
            viewModel.calendarSelectedDay = day.calendar.time
            setDayRoutes(day.calendar)
            setWorkGroups(day.calendar)
        }

        shuttleReservationAdapter = ShuttleReservationAdapter(object : ShuttleReservationAdapter.ShuttleReservationItemClickListener{
            override fun onCancelClicked(model: ShuttleNextRide) {
                FlexigoInfoDialog.Builder(requireContext())
                        .setTitle(getString(R.string.shuttle_demand_cancel))
                        .setText1(getString(R.string.shuttle_cancel_text, model.firstDepartureDate.convertToShuttleReservationTime2(), model.name ?: ""))
                        .setCancelable(false)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.Generic_Continue)) { dialog ->
                            dialog.dismiss()
                            viewModel.changeShuttleSelectedDate(model, Date(model.firstDepartureDate).convertForBackend2(), null, null)

                        }
                        .setCancelButton(getString(R.string.Generic_Close)) { dialog ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
            }

            override fun onEditClicked(model: ShuttleNextRide) {
                for (item in tempDayModelWorkGroupInstancesFiltered){
                    if (model.workgroupInstanceId == item.id){
                        item.firstDepartureDate = model.firstDepartureDate
                        viewModel.workgroupInstance = item
                        viewModel.workgroupTemplate = viewModel.getTemplateForInstance(item)
                    }
                }

                viewModel.isFromCampus = (model.fromType == FromToType.CAMPUS || model.fromType == FromToType.PERSONNEL_WORK_LOCATION)
                viewModel.isMultipleHours = false
                viewModel.isReturningShuttlePlanningEdit = false
                viewModel.openBottomSheetEditShuttle.value = true
            }

        })

        shuttleWorkgroupInstanceAdapter = ShuttleWorkgroupInstanceAdapter(object : ShuttleWorkgroupInstanceAdapter.WorkGroupInstanceItemClickListener {
            override fun onItemClicked(workgroupInstance: WorkGroupInstance) {
                viewModel.workgroupInstance = workgroupInstance
                viewModel.workgroupTemplate = viewModel.getTemplateForInstance(workgroupInstance)

                viewModel.workgroupType.value = viewModel.workgroupTemplate?.workgroupType ?: "SHUTTLE"
                var departureText = viewModel.workgroupTemplate?.shift?.arrivalHour.convertHourMinutes() ?: ""
                viewModel.workgroupTemplate?.let {
                    if (it.direction == WorkgroupDirection.ROUND_TRIP) {
                        departureText = ((it.shift?.departureHour ?: it.shift?.arrivalHour).convertHourMinutes() ?: "") + "-" + ((it.shift?.returnDepartureHour ?: it.shift?.returnArrivalHour).convertHourMinutes() ?: "")
                    }
                }

                val name = if (viewModel.workgroupInstance?.name!!.contains("["))
                    viewModel.workgroupInstance?.name!!.split("[")[0]
                else
                    viewModel.workgroupInstance?.name!!

                val dialogText = String.format(getString(R.string.shuttle_demand_info), name, departureText)

                viewModel.isFromCampus = (viewModel.workgroupTemplate?.fromType == FromToType.CAMPUS || viewModel.workgroupTemplate?.fromType == FromToType.PERSONNEL_WORK_LOCATION)

                var isMultiHour = false
                viewModel.workGroupSameNameList.value!!.find {
                    it.name == viewModel.workgroupInstance?.name!! }?.let {
                    isMultiHour = true
                } ?: run {
                }

                if (isMultiHour){
                    viewModel.isMultipleHours = true
                    viewModel.isReturningShuttlePlanningEdit = false
                    viewModel.openBottomSheetEditShuttle.value = true
                } else{
                    viewModel.isMultipleHours = false

                        if (workgroupInstance.workgroupStatus == WorkgroupStatus.PENDING_DEMAND) {
                                CalendarSendDemandWorkgroupDialog(requireContext(), workgroupInstance, dialogText, object : CalendarSendDemandWorkgroupDialog.CalendarDialogListener {
                                    override fun sendDemandRequestWorkgroup(workgroupInstance: WorkGroupInstance, dialog: Dialog) {
                                        dialog.dismiss()
                                        viewModel.demandWorkgroup(WorkgroupDemandRequest(
                                                workgroupInstanceId = workgroupInstance.id,
                                                stationId = null,
                                                location = null
                                        ))

                                    }
                                }).show()
                            } else {
                                val from: SearchRequestModel
                                val to: SearchRequestModel
                                if (viewModel.workgroupTemplate?.fromType == FromToType.CAMPUS || viewModel.workgroupTemplate?.fromType == FromToType.PERSONNEL_WORK_LOCATION) {
                                    to = SearchRequestModel(
                                        lat = AppDataManager.instance.personnelInfo?.homeLocation?.latitude,
                                        lng = AppDataManager.instance.personnelInfo?.homeLocation?.longitude,
                                        destinationId = null
                                    )
                                    from = SearchRequestModel(
                                        lat = null,
                                        lng = null,
                                        destinationId = viewModel.workgroupTemplate?.fromTerminalReferenceId
                                    )
                                }
                                else {
                                    from = SearchRequestModel(
                                        lat = AppDataManager.instance.personnelInfo?.homeLocation?.latitude,
                                        lng = AppDataManager.instance.personnelInfo?.homeLocation?.longitude,
                                        destinationId = null
                                    )
                                    to = SearchRequestModel(
                                        lat = null,
                                        lng = null,
                                        destinationId = viewModel.workgroupTemplate?.toTerminalReferenceId
                                    )
                                }

                                viewModel.getStops(
                                        RouteStopRequest(
                                                from = from,
                                                whereto = to,
                                                shiftId = null,
                                                workgroupInstanceId = workgroupInstance.id
                                        ),
                                        true,
                                        requireContext()
                                )
                            }
                }

            }
        })
        shuttleDemandAdapter = ShuttleDemandAdapter()

        shuttleRegularRoutesAdapter = ShuttleRegularRoutesAdapter(object : ShuttleRegularRoutesAdapter.ShuttleRegularRouteItemClickListener{
            override fun onCancelClicked(model: ShuttleNextRide) {

                var title = getString(R.string.wont_use_shuttle_2)

                if (model.notUsing) {
                    title = getString(R.string.use_shuttle)
                }

                val dialog = AlertDialog.Builder(requireContext())
                dialog.setCancelable(false)
                dialog.setTitle(fromHtml("<b>$title</b>"))
                dialog.setMessage(resources.getString(R.string.not_attending_selection_message))
                dialog.setPositiveButton(resources.getString(R.string.selected_date)) { d, _ ->
                    d.dismiss()
                    viewModel.changeShuttleSelectedDate(model, null, null, true)
                }
                dialog.setNegativeButton(resources.getString(R.string.multiple_dates)) { d, _ ->
                    d.dismiss()
                    viewModel.calendarSelectedRides.value = model
                    viewModel.openBottomSheetCalendar.value = true

                }
                dialog.setNeutralButton(resources.getString(R.string.cancel)) { d, _ ->
                    d.dismiss()
                }

                dialog.show()

            }

            override fun onEditClicked(model: ShuttleNextRide) {
                for (item in tempDayModelWorkGroupInstancesFiltered){
                    if (model.workgroupInstanceId == item.id){
                        item.firstDepartureDate = model.firstDepartureDate
                        viewModel.workgroupInstance = item
                        viewModel.workgroupTemplate = viewModel.getTemplateForInstance(item)
                    }
                }

                viewModel.isFromCampus = (model.fromType == FromToType.CAMPUS || model.fromType == FromToType.PERSONNEL_WORK_LOCATION)
                viewModel.isMultipleHours = false
                viewModel.isReturningShuttlePlanningEdit = false
                viewModel.openBottomSheetEditShuttle.value = true
            }

        })

        binding.recyclerViewReservations.adapter = shuttleReservationAdapter
        binding.recyclerViewWorkgroups.adapter = shuttleWorkgroupInstanceAdapter
        binding.recyclerViewDemands.adapter = shuttleDemandAdapter
        binding.recyclerviewRegularRoutes.adapter = shuttleRegularRoutesAdapter

        viewModel.getStopsResponse.observe(viewLifecycleOwner) { response ->

            if (response != null) {
                val routeMap = mutableMapOf<Long, StationModel>()
                response.response.forEach { station ->
                    if (routeMap.containsKey(station.routeId)) {
                        val currentStation = routeMap[station.routeId]

                        if ((station.route?.route?.durationInMin
                                        ?: Int.MAX_VALUE) < ((currentStation?.route?.route?.durationInMin
                                ?: Int.MAX_VALUE) as Nothing)) {
                            routeMap[station.routeId] = station
                        }

                    } else {
                        routeMap[station.routeId] = station
                    }

                }

                val stations = mutableListOf<StationModel>()

                routeMap.forEach { (_, stationModel) ->
                    stations.add(stationModel)
                }


                val textToShow = (viewModel.selectedFromLocation?.text
                        ?: viewModel.selectedFromDestination?.title)
                        .plus(" - ")
                        .plus(viewModel.selectedToLocation?.text
                                ?: viewModel.selectedToDestination?.title)


                if (textToShow.contains("null")) {
                    viewModel.textViewBottomSheetRoutesFromToName.value = "Normal"
                }
                else
                    viewModel.textViewBottomSheetRoutesFromToName.value = textToShow

                viewModel.openBottomSheetRoutes.value = true
                viewModel.getStopsResponse.value = null

            }

        }

        viewModel.searchedRoutes.observe(viewLifecycleOwner) { routes ->

            if (routes != null) {

                val textToShow = (viewModel.selectedFromLocation?.text
                    ?: viewModel.selectedFromDestination?.title)
                    .plus(" - ")
                    .plus(viewModel.selectedToLocation?.text
                        ?: viewModel.selectedToDestination?.title)


                if (textToShow.contains("null")) {
                    viewModel.textViewBottomSheetRoutesFromToName.value = "Normal"
                }
                else
                    viewModel.textViewBottomSheetRoutesFromToName.value = textToShow

                viewModel.searchRoutesAdapterSetListTrigger.value = routes.toMutableList()

                viewModel.isReturningShuttleEdit = true

                viewModel.openBottomSheetRoutes.value = true

            }
        }

        viewModel.myNextRides.observe(viewLifecycleOwner) { myNextRides ->

            val eventDays = mutableListOf<EventDay>()

            dayModelMap = mutableMapOf()

            myNextRides.forEach { myNextRide ->

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = myNextRide.firstDepartureDate
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val startOfDay = calendar.timeInMillis

                val shuttleDayModel: ShuttleDayModelFromNextRides? =

                        if (dayModelMap.containsKey(startOfDay)) {
                            dayModelMap[startOfDay]

                        } else {

                            val model = ShuttleDayModelFromNextRides(
                                    usualRides = mutableListOf(),
                                    reservations = mutableListOf(),
                                    demands = mutableListOf()
                            )
                            dayModelMap[startOfDay] = model

                            model
                        }

                shuttleDayModel?.let { model ->
                    if (myNextRide.reserved || myNextRide.workgroupStatus == WorkgroupStatus.PENDING_DEMAND || myNextRide.routeId == null) {
                        model.reservations.add(myNextRide)
                    } else {
                        val instanceAvailable = model.usualRides.any { it.workgroupInstanceId == myNextRide.workgroupInstanceId }
                        if (!instanceAvailable) {
                            model.usualRides.add(myNextRide)
                        }
                        model
                    }

                }
            }

            dayModelMap.forEach { (dayLong, model) ->

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dayLong

                var pointCount = 0

                if (model.usualRides.isNotEmpty()) {

                    pointCount += 1
                }
                if (model.reservations.isNotEmpty()) {
                    pointCount += 1
                }

                when (pointCount) {
                    1 -> {
                        if(DateUtils.isToday(calendar.timeInMillis))
                            eventDays.add(EventDay(calendar, R.drawable.shuttle_day_icon_blue_4dp))
                        else
                            eventDays.add(EventDay(calendar, R.drawable.shuttle_day_icon_4dp))
                    }
                    2 -> {
                        eventDays.add(EventDay(calendar, R.drawable.shuttle_day_double_icon))
                    }
                }

            }

            binding.calendarView.setEvents(eventDays)

            setDayRoutes(binding.calendarView.selectedDates[0])

        }

        viewModel.allNextRides.value?.let { myNextRides ->

            val eventDays = mutableListOf<EventDay>()

            dayModelMapAllRides = mutableMapOf()

            myNextRides.forEach { myNextRide ->

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = myNextRide.firstDepartureDate
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val startOfDay = calendar.timeInMillis

                val shuttleDayModel: ShuttleDayModelFromNextRides? =

                        if (dayModelMapAllRides.containsKey(startOfDay)) {

                            dayModelMapAllRides[startOfDay]

                        } else {

                            val model = ShuttleDayModelFromNextRides(
                                    usualRides = mutableListOf(),
                                    reservations = mutableListOf(),
                                    demands = mutableListOf()
                            )
                            dayModelMapAllRides[startOfDay] = model

                            model
                        }


                // for reservations
                shuttleDayModel?.let { model ->
                    if (myNextRide.reserved || myNextRide.routeId == null) {

                        val instanceAvailable = model.reservations.any { it.workgroupInstanceId == myNextRide.workgroupInstanceId }
                        if (!instanceAvailable) {
                            model.reservations.add(myNextRide)
                        }
                    }

                }

                // for regular shuttle
                shuttleDayModel?.let { model ->
                    if (!myNextRide.reserved && myNextRide.routeId != null) {

                        val instanceAvailable = model.usualRides.any { it.workgroupInstanceId == myNextRide.workgroupInstanceId }
                        if (!instanceAvailable) {
                            model.usualRides.add(myNextRide)
                        }
                    }

                }
            }

            dayModelMapAllRides.forEach { (dayLong, model) ->

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dayLong

                var pointCount = 0

                if (model.demands.isNotEmpty() || model.usualRides.isNotEmpty()) {

                    pointCount += 1
                }
                if (model.reservations.isNotEmpty()) {
                    pointCount += 1
                }

                when (pointCount) {
                    1 -> {
                        if(DateUtils.isToday(calendar.timeInMillis))
                            eventDays.add(EventDay(calendar, R.drawable.shuttle_day_icon_blue_4dp))
                        else
                            eventDays.add(EventDay(calendar, R.drawable.shuttle_day_icon_4dp))
                    }
                    2 -> {
                        eventDays.add(EventDay(calendar, R.drawable.shuttle_day_double_icon))
                    }
                }

            }

            binding.calendarViewSelectDay.setEvents(eventDays)

        }

        if (viewModel.getShuttleUseDaysResponse.value == null || viewModel.getShuttleUseDaysResponse.value?.response?.isEmpty()?.not() == false || dayModelWorkgroupsInstances?.isEmpty() == true) {
            binding.textViewNoPlannedRoute.visibility = View.VISIBLE
            binding.layoutDayRouteList.visibility = View.GONE
        }

        viewModel.routesDetails.observe(viewLifecycleOwner) { routeModels ->
            if (routeModels.size == 1) {
                val routeModel = routeModels[0]
                currentRoute = routeModel

                fillUI(routeModel)

            }
        }

        viewModel.cancelDemandWorkgroupResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.cancelDemandWorkgroupResponse.value = null

                FlexigoInfoDialog.Builder(requireContext())
                        .setTitle(getString(R.string.title_success))
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

                            viewModel.getMyNextRides()

                        }
                        .create()
                        .show()

                }
            }

        }

        viewModel.fillUITrigger.observe(viewLifecycleOwner) {
            if(it != null) {
                fillUI(it)
                viewModel.fillUITrigger.value = null
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.isShuttleServicePlaningFragment = false
        viewModel.searchedRoutes.value = null
        viewModel.searchRoutesAdapterSetMyDestinationTrigger.value = null
        viewModel.searchRoutesAdapterSetListTrigger.value = null
        viewModel.clearSelections()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.isShuttleServicePlaningFragment = true
    }

    private fun setWorkGroups(calendar: Calendar) {
        val selectedDateLong = calendar.timeInMillis
        val tempWorkGroupSameNameList = mutableListOf<WorkGroupInstance>()
        val tempWorkGroupTemplateSameNameList = mutableListOf<WorkGroupTemplate>()
        tempDayModelWorkGroupInstancesFiltered = mutableListOf()

        dayModelWorkgroupsInstances?.forEach { workgroup ->

            if (workgroup.endDate != null) {

                workgroup.startDate.let {

                    val template = viewModel.getTemplateForInstance(workgroup)

                    if (template != null) {
                        template.shift?.let { it1 ->
                            if (viewModel.checkIncludesRecurringDay(it1, calendar.time.convertForWeekDaysLiteral())) {
                                if ((selectedDateLong >= workgroup.startDate!!) && (selectedDateLong <= workgroup.endDate)) {
                                    tempWorkGroupTemplateSameNameList.add(template)

                                    tempDayModelWorkGroupInstancesFiltered.find {
                                        it.name == workgroup.name }?.let {
                                        tempWorkGroupSameNameList.add(workgroup)
                                    } ?: run {
                                        tempDayModelWorkGroupInstancesFiltered.add(workgroup)
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                workgroup.startDate.let {
                    val template = viewModel.getTemplateForInstance(workgroup)
                    template?.shift?.let { it1 ->

                        if (viewModel.checkIncludesRecurringDay(it1, calendar.time.convertForWeekDaysLiteral())) {
                            if (selectedDateLong > workgroup.startDate!!) {
                                tempWorkGroupTemplateSameNameList.add(template)

                                tempDayModelWorkGroupInstancesFiltered.find {
                                    it.name == workgroup.name }?.let {
                                    tempWorkGroupSameNameList.add(workgroup)
                                } ?: run {
                                    tempDayModelWorkGroupInstancesFiltered.add(workgroup)
                                }
                            }
                        }
                    }
                }
            }

        }

        tempDayModelWorkGroupInstancesFiltered.forEach { workgroupInstance ->
            tempWorkGroupSameNameList.find {
                it.name == workgroupInstance.name }?.let {
                tempWorkGroupSameNameList.add(0, workgroupInstance)
            } ?: run {}
        }

        viewModel.workGroupSameNameList.value = tempWorkGroupSameNameList
        viewModel.workgroupTemplateList.value = tempWorkGroupTemplateSameNameList

        if (tempDayModelWorkGroupInstancesFiltered.isNotEmpty()) {
            binding.textViewNoPlannedRoute.visibility = View.GONE
            binding.layoutDayRouteList.visibility = View.VISIBLE
            binding.textViewWorkgroupInfo.visibility = View.VISIBLE
        } else {
            binding.textViewWorkgroupInfo.visibility = View.GONE
        }

        dayModelWorkgroupsTemplates?.let {
            shuttleWorkgroupInstanceAdapter?.setList(tempDayModelWorkGroupInstancesFiltered,
                it, tempWorkGroupSameNameList
            )
        }



    }

    private  fun workgroupsEmpty() {
        binding.recyclerviewRegularRoutes.visibility = View.GONE
        binding.textViewRegularRouteInfo.visibility = View.GONE
        binding.dividerWorkgroup.visibility = View.GONE
        binding.dividerReservation.visibility = View.GONE
        binding.dividerAfterRegularRoute.visibility = View.GONE
        binding.dividerAfterDemand.visibility = View.GONE
        binding.textViewReservationInfo.visibility = View.GONE
        binding.dividerReservation.visibility = View.GONE
        binding.recyclerViewReservations.visibility = View.GONE
        binding.txtDemandRequestInfo.visibility = View.GONE
        binding.dividerWorkgroup.visibility = View.GONE
    }

    private fun setDayRoutes(calendar: Calendar) {
        val model = dayModelMap[calendar.timeInMillis]

        if (model == null) {
            if (dayModelWorkgroupsInstances?.isNotEmpty() == true) {
                workgroupsEmpty()
            }
            else {
                binding.textViewNoPlannedRoute.visibility = View.VISIBLE
                binding.layoutDayRouteList.visibility = View.GONE
            }

        }

        model?.let {
            val reservations: MutableList<ShuttleNextRide> = mutableListOf()
            model.reservations.forEach { reservation ->
                if (reservations.any {
                        it.workgroupInstanceId == reservation.workgroupInstanceId
                    }) {
                }
                else {
                    reservations.add(reservation)
                }
            }

            model.reservations = reservations
            shuttleReservationAdapter?.setList(model.reservations)

            if (model.usualRides.isEmpty() && model.reservations.isEmpty() && model.demands.isEmpty()) {
                binding.textViewNoPlannedRoute.visibility = View.VISIBLE
                binding.layoutDayRouteList.visibility = View.GONE
                binding.txtDemandRequestInfo.visibility = View.GONE
                binding.textViewReservationInfo.visibility = View.GONE
            } else {
                binding.textViewNoPlannedRoute.visibility = View.GONE
                binding.layoutDayRouteList.visibility = View.VISIBLE
                binding.txtDemandRequestInfo.visibility = View.VISIBLE
                binding.textViewReservationInfo.visibility = View.VISIBLE

            }

            if (model.usualRides.isNotEmpty()) {

                shuttleRegularRoutesAdapter?.setList(model.usualRides)

                binding.recyclerviewRegularRoutes.visibility = View.VISIBLE
                binding.textViewRegularRouteInfo.visibility = View.VISIBLE
            }
            else {
                binding.recyclerviewRegularRoutes.visibility = View.GONE
                binding.textViewRegularRouteInfo.visibility = View.GONE

            }

            if (model.reservations.isEmpty()) {
                binding.dividerWorkgroup.visibility = View.INVISIBLE
                binding.dividerReservation.visibility = View.GONE
                binding.dividerAfterRegularRoute.visibility = View.GONE
                binding.dividerAfterDemand.visibility = View.GONE
                binding.textViewReservationInfo.visibility = View.GONE
                binding.dividerReservation.visibility = View.GONE
                binding.recyclerViewReservations.visibility = View.GONE
            } else {
                binding.dividerReservation.visibility = View.VISIBLE
                binding.dividerAfterRegularRoute.visibility = View.VISIBLE
                binding.dividerAfterDemand.visibility = View.VISIBLE
                binding.textViewReservationInfo.visibility = View.VISIBLE
                binding.dividerReservation.visibility = View.VISIBLE
                binding.recyclerViewReservations.visibility = View.VISIBLE
            }

            if (model.demands.isEmpty()) {
                binding.dividerWorkgroup.visibility = View.INVISIBLE
                binding.dividerAfterDemand.visibility = View.GONE
                binding.dividerAfterRegularRoute.visibility = View.GONE
                binding.txtDemandRequestInfo.visibility = View.GONE
                binding.recyclerViewDemands.visibility = View.GONE
            } else {
                binding.dividerAfterDemand.visibility = View.VISIBLE
                binding.txtDemandRequestInfo.visibility = View.VISIBLE
                binding.dividerAfterRegularRoute.visibility = View.VISIBLE
                binding.recyclerViewDemands.visibility = View.VISIBLE
            }

        }
    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "ShuttleServicePlanning"

        fun newInstance() = ShuttleServicePlanningFragment()

    }

    data class ShuttleDayModelFromNextRides(
        val usualRides: MutableList<ShuttleNextRide>,
        var reservations: MutableList<ShuttleNextRide>,
        val demands: MutableList<ShuttleNextRide>
    )

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

    override fun onLocationPermissionOk() {

        locationClient = FusedLocationClient(requireContext())

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            @SuppressLint("MissingPermission")
            override fun onLocationUpdated(location: Location) {

                myLocation = location
                viewModel.myLocation = location

                locationClient.stop()

            }

            override fun onLocationFailed(message: String) {

                if (activity?.isFinishing != false || activity?.isDestroyed != false) {
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

    override fun onLocationPermissionFailed() {
    }

    private fun fillUI(routeModel: RouteModel) {
        viewModel.searchRoutesAdapterSetMyDestinationTrigger.value = routeModel.destination
    }

}