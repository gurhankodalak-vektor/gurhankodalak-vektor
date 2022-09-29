package com.vektortelekom.android.vservice.ui.route.search

import android.content.Context
import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.gson.JsonArray
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupShift
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupTemplate
import com.vektortelekom.android.vservice.data.model.workgroup.WorkgroupResponse
import com.vektortelekom.android.vservice.data.repository.ShuttleRepository
import com.vektortelekom.android.vservice.data.repository.TicketRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.ui.route.RouteNavigator
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.utils.convertForBackend2
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class RouteSearchViewModel
@Inject
constructor(
    private val shuttleRepository: ShuttleRepository,
    private val ticketRepository: TicketRepository,
    private val scheduler: SchedulerProvider): BaseViewModel<RouteNavigator>() {

    var zoomStation = false
    var selectedStation : StationModel? = null

    var myLocation: Location? = null
    var workLocation: LatLng? = null
    val routeSelectedForReservation: MutableLiveData<RouteModel> = MutableLiveData()
    var searchedRoutePreview = MutableLiveData<RouteModel>()

    val routeSortList = listOf(ShuttleViewModel.RouteSortType.WalkingDistance, ShuttleViewModel.RouteSortType.TripDuration, ShuttleViewModel.RouteSortType.OccupancyRatio)

    val selectedRouteSortItemIndexTrigger: MutableLiveData<Int> = MutableLiveData()
    var selectedRouteSortItemIndex : Int? = null

    val selectedCalendarDay: MutableLiveData<Long> =  MutableLiveData()
    val selectedStartDay: MutableLiveData<String> =  MutableLiveData()
    val selectedStartDayCalendar: MutableLiveData<Date> =  MutableLiveData()
    val selectedFinishDayCalendar: MutableLiveData<Date> =  MutableLiveData()
    val selectedFinishDay: MutableLiveData<String> =  MutableLiveData()
    val selectedTime: MutableLiveData<Long> =  MutableLiveData()

    var dateAndWorkgroupList: List<DateAndWorkgroup>? = null

    val openNumberPicker: MutableLiveData<SelectType> = MutableLiveData()

    val openBottomSheetSearchLocation: MutableLiveData<Boolean> = MutableLiveData()
    val bottomSheetBehaviorEditShuttleState: MutableLiveData<Int> = MutableLiveData()

    val myNextRides: MutableLiveData<List<ShuttleNextRide>> = MutableLiveData()
    val destinations: MutableLiveData<List<DestinationModel>> = MutableLiveData()

    val allWorkgroup: MutableLiveData<List<ShuttleNextRide>> = MutableLiveData()

    var currentWorkgroup : MutableLiveData<ShuttleNextRide> = MutableLiveData()

    var toLabelText : MutableLiveData<String> = MutableLiveData()
    var fromLabelText : MutableLiveData<String> = MutableLiveData()
    var dateValueText : MutableLiveData<String> = MutableLiveData()
    var campusAndLocationName : MutableLiveData<String> = MutableLiveData()
    var departureArrivalTimeText : MutableLiveData<String> = MutableLiveData()
    var departureArrivalTimeTextPopup : MutableLiveData<String> = MutableLiveData()
    var routeTitle : MutableLiveData<String> = MutableLiveData()
    var routeName : MutableLiveData<String> = MutableLiveData()

    var toLocation : MutableLiveData<LocationModel> = MutableLiveData()
    var fromLocation : MutableLiveData<LocationModel> = MutableLiveData()

    var toIcon : MutableLiveData<Int> = MutableLiveData()
    var fromIcon : MutableLiveData<Int> = MutableLiveData()

    val demandWorkgroupResponse: MutableLiveData<BaseResponse> = MutableLiveData()

    var selectedToLocation: ShuttleViewModel.FromToLocation? = null

    var selectedFromDestination: DestinationModel? = null
    var selectedFromDestinationIndex: Int? = null
    var destinationId: Long? = null
    var fromToType: FromToType? = null

    val homeLocation : MutableLiveData<LatLng> = MutableLiveData()

    val isLocationToHome : MutableLiveData<Boolean> = MutableLiveData()
    val isFromEditPage : MutableLiveData<Boolean> = MutableLiveData()
    val isFromChanged : MutableLiveData<Boolean> = MutableLiveData()

    var selectedDate: DateAndWorkgroup? = null
    var pickerTitle: String? = null
    var selectedDateIndex: Int? = null
    var mode: String? = null

    var isSelectedTime:  MutableLiveData<Boolean> = MutableLiveData()
    var textviewDepartureTime:  MutableLiveData<String> = MutableLiveData()

    val autocompletePredictions: MutableLiveData<List<AutocompletePrediction>> = MutableLiveData()

    val searchRoutesAdapterSetListTrigger: MutableLiveData<MutableList<RouteModel>> = MutableLiveData()

    var searchedStops = MutableLiveData<List<StationModel>?>()
    var searchedRoutes = MutableLiveData<List<RouteModel>?>()

    var currentWorkgroupResponse = MutableLiveData<WorkgroupResponse>()
    val updatePersonnelStationResponse: MutableLiveData<Boolean> = MutableLiveData()

    val daysValues: MutableLiveData<JsonArray> = MutableLiveData()
    val daysLocalValuesMap: MutableLiveData<HashMap<Int, String>> = MutableLiveData()

    val successReservation: MutableLiveData<Boolean> = MutableLiveData()

    var reservationCancelled: MutableLiveData<BaseResponse> = MutableLiveData()

    enum class SelectType {
        Time,
        RouteSorting,
        CampusFrom
    }

    data class DateAndWorkgroup(
        var date: Long,
        val workgroupId: Long,
        val workgroupStatus: WorkgroupStatus?,
        val fromType: FromToType,
        val fromTerminalReferenceId: Long?,
        val ride: ShuttleNextRide,
        val template: WorkGroupTemplate?
    )
    fun isFirstLeg(direction: WorkgroupDirection, fromType: FromToType) : Boolean {
        if (direction == WorkgroupDirection.ONE_WAY)
            return true

        return  fromType == FromToType.PERSONNEL_SHUTTLE_STOP || fromType == FromToType.PERSONNEL_HOME_ADDRESS
    }


    fun checkIncludesRecurringDay(workGroupShift: WorkGroupShift, selectedDay: String) =
        when (selectedDay.lowercase()) {
            "monday" -> {
                workGroupShift.monday
            }
            "tuesday" -> {
                workGroupShift.tuesday
            }
            "wednesday" -> {
                workGroupShift.wednesday
            }
            "thursday" -> {
                workGroupShift.thursday
            }
            "friday" -> {
                workGroupShift.friday
            }
            "saturday" -> {
                workGroupShift.saturday
            }
            "sunday" -> {
                workGroupShift.sunday
            }
            else -> {
                false
            }
        }

    fun cancelShuttleDemand(request: WorkgroupDemandRequest) {

        compositeDisposable.add(
            shuttleRepository.cancelDemandWorkgroup(request)
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    reservationCancelled.value = response
                }, { ex ->
                    println("error: ${ex.localizedMessage}")
                    setIsLoading(false)
                    navigator?.handleError(ex)
                }, {
                    setIsLoading(false)
                }, {
                    setIsLoading(true)
                }
                )
        )
    }

    fun cancelShuttleReservation(request: ShuttleReservationRequest2) {
        compositeDisposable.add(
            shuttleRepository.shuttleReservation2(request)
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->

                    if (response.error != null) {
                        navigator?.handleError(Exception(response.error?.message))
                    } else {
                        reservationCancelled.value = response

                    }

                }, { ex ->
                    println("error: ${ex.localizedMessage}")
                    setIsLoading(false)
                    navigator?.handleError(ex)
                }, {
                    setIsLoading(false)
                }, {
                    setIsLoading(true)
                }
                )
        )
    }

    fun getReservationRequestModel(stop: StationModel) : ShuttleReservationRequest3? {
            currentWorkgroupResponse.value?.instance?.let { instance ->
                currentWorkgroupResponse.value?.template?.let { template ->
                    val useFirstLeg = currentWorkgroup.value!!.firstLeg
                    val useReturnLeg = template.direction == WorkgroupDirection.ROUND_TRIP
                    return ShuttleReservationRequest3(
                        reservationDay = selectedStartDayCalendar.value!!.convertForBackend2(),
                        reservationDayEnd= selectedFinishDayCalendar.value.convertForBackend2(),
                        workgroupInstanceId = instance.id,
                        routeId = stop.routeId ?:0L,
                        useFirstLeg = useFirstLeg,
                        firstLegStationId = if (useFirstLeg == true) selectedStation?.id else null,
                        useReturnLeg = useReturnLeg,
                        returnLegStationId = if (useReturnLeg == true) stop.id else null,
                        dayOfWeeks = daysValues.value
                    )
                }

            }

        return  null
    }

    fun setDataFromShuttle(current: ShuttleNextRide){
        currentWorkgroup.value = current
    }

    fun makeShuttleReservation(request: ShuttleReservationRequest3) {
        compositeDisposable.add(
            shuttleRepository.shuttleReservation3(request)
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    if (response.error != null) {
                        navigator?.handleError(Exception(response.error?.message))
                    } else {
                        successReservation.value = true
                    }

                }, { ex ->
                    println("error: ${ex.localizedMessage}")
                    setIsLoading(false)
                    navigator?.handleError(ex)
                }, {
                    setIsLoading(false)
                }, {
                    setIsLoading(true)
                }
                )
        )
    }
    fun updatePersonnelStation(id: Long) {
        compositeDisposable.add(
            shuttleRepository.updatePersonnelStation(id)
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    if (response.error != null) {
                        navigator?.handleError(Exception(response.error?.message))
                    } else {
                        updatePersonnelStationResponse.value = true
                    }
                }, { ex ->
                    println("error: ${ex.localizedMessage}")
                    setIsLoading(false)
                    navigator?.handleError(ex)
                }, {
                    setIsLoading(false)
                }, {
                    setIsLoading(true)
                }
                )
        )
    }

    fun getWorkgroupInformation(workgroupInstanceId: Long) {

        compositeDisposable.add(
            shuttleRepository.getWorkgroupInformation(workgroupInstanceId)
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    currentWorkgroupResponse.value = response
                }, { ex ->
                    println("error: ${ex.localizedMessage}")
                }, {
                    setIsLoading(false)
                }, {
                    setIsLoading(true)
                }
                )
        )
    }

    fun getStops(request: RouteStopRequest, context: Context? = null) {
        compositeDisposable.add(
            shuttleRepository.getStops(request)
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    if (response != null) {
                        val routeIdList = mutableListOf<Long>()

                        (response as List<StationModel>).forEach { station ->
                            if (routeIdList.contains(station.routeId).not()) {
                                routeIdList.add(station.routeId)
                            }
                        }
                        searchedStops.value = response
                        getRoutesDetailsWith(RoutesDetailRequestModel(routeIdList), context)
                    }
                }, { ex ->
                    println("error: ${ex.localizedMessage}")
                    navigator?.handleError(Throwable(context?.getString(R.string.opt_time_over)))
                }, {
                }, {
                }
                )
        )
    }


    private fun getRoutesDetailsWith(routesRequest: RoutesDetailRequestModel, context: Context? = null) {
        compositeDisposable.add(
            shuttleRepository.getRoutesDetailsWith(routesRequest)
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    val calculatedRoutes = mutableListOf<RouteModel>()
                    response.response.forEach { route ->
                        var closestStop: StationModel? = null

                        searchedStops.value?.forEach { stop ->
                            if (stop.routeId == route.id) {
                                if (closestStop == null) {
                                    closestStop = stop
                                }
                            }
                        }

                        route.closestStation = closestStop
                        calculatedRoutes.add(route)
                    }

                    searchedRoutes.value = calculatedRoutes

                }, { ex ->
                    println("error: ${ex.localizedMessage}")
                    navigator?.handleError(Throwable(context?.getString(R.string.opt_time_over)))
                }, {
                }, {
                }
                )
        )
    }

    fun getDestinations() {
        compositeDisposable.add(
            ticketRepository.getDestinations()
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    destinations.value = response.response
                }, { ex ->
                    AppLogger.e(ex, "operation failed.")
                }, {
                }, {
                }
                )
        )
    }
    fun demandWorkgroup(request: WorkgroupDemandRequest) {

        compositeDisposable.add(
            shuttleRepository.demandWorkgroup(request)
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->

                    demandWorkgroupResponse.value = response
                }, { ex ->
                    println("error: ${ex.localizedMessage}")
                    setIsLoading(false)
                    navigator?.handleError(ex)
                }, {
                    setIsLoading(false)
                }, {
                    setIsLoading(true)
                }
                )
        )
    }

    fun getMyNextRides() {

        compositeDisposable.add(
            shuttleRepository.getMyNextRides()
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    myNextRides.value = response
                }, { ex ->
                    println("error: ${ex.localizedMessage}")
                    navigator?.handleError(ex)
                }, {
                }, {
                }
                )
        )
    }

    fun getAllNextRides() {

        compositeDisposable.add(
            shuttleRepository.getAllNextRides()
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    allWorkgroup.value = response
                    getDestinations()
                }, { ex ->
                    println("error: ${ex.localizedMessage}")
                    navigator?.handleError(ex)
                }, {
                }, {
                }
                )
        )
    }

}