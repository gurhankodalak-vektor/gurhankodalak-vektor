package com.vektortelekom.android.vservice.ui.shuttle

import android.content.Context
import android.location.Location
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupInstance
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupShift
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupTemplate
import com.vektortelekom.android.vservice.data.model.workgroup.WorkgroupResponse
import com.vektortelekom.android.vservice.data.repository.ShuttleRepository
import com.vektortelekom.android.vservice.data.repository.TicketRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.ui.shuttle.model.VPlaceModel
import com.vektortelekom.android.vservice.utils.convertForBackend
import com.vektortelekom.android.vservice.utils.convertForBackend2
import com.vektortelekom.android.vservice.utils.convertForDate
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject

class ShuttleViewModel
@Inject
constructor(private val shuttleRepository: ShuttleRepository,
            private val userRepository: UserRepository,
            private val ticketRepository: TicketRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<ShuttleNavigator>() {

    val routeDetails: MutableLiveData<RouteModel> = MutableLiveData()
    val routeResponse: MutableLiveData<RouteResponse> = MutableLiveData()

    val myRouteDetails: MutableLiveData<RouteModel> = MutableLiveData()

    val routeDetailsForAttendenceReservation: MutableLiveData<RouteModel> = MutableLiveData()

    val vehicleLocation: MutableLiveData<VehicleLocationResponse?> = MutableLiveData()

    val vehicleErrorMessage: MutableLiveData<String> = MutableLiveData()

    val personnelDetailsResponse: MutableLiveData<PersonelInfoResponse> = MutableLiveData()

    val getShuttleUseDaysResponse: MutableLiveData<GetShuttleUseDaysResponse> = MutableLiveData()

    val updateShuttleDayResult: MutableLiveData<Boolean> = MutableLiveData()

    val isQrCodeOk: MutableLiveData<Boolean> = MutableLiveData()

    val routeSelectedForReservation: MutableLiveData<RouteModel> = MutableLiveData()

    val currentDay: MutableLiveData<Long> =  MutableLiveData()
    val driverStationList: MutableLiveData<List<StationModel>> =  MutableLiveData()

    private val dayInterval = 15
    var startDate: Date = Date()
    var endDate: Date = DateTime(startDate).plusDays(dayInterval).toDate()

    var fromPlace: MutableLiveData<VPlaceModel> = MutableLiveData()

    var toPlace: MutableLiveData<VPlaceModel> = MutableLiveData()

    var workLocation: LatLng? = null
    var isMultipleHours = false

    val destinations: MutableLiveData<List<DestinationModel>> = MutableLiveData()

    val workGroupSameNameList: MutableLiveData<List<WorkGroupInstance>> = MutableLiveData()

    var currentSearchType: SearchType = SearchType.from

    val getStopsResponse: MutableLiveData<RouteStopResponse> = MutableLiveData()

    val stopDetailsResponse: MutableLiveData<RouteDetailResponse> = MutableLiveData()

    val updatePersonnelStationResponse: MutableLiveData<Boolean> = MutableLiveData()

    var myLocation: Location? = null

    var waitingForSearchResponse = false

    val autocompletePredictions: MutableLiveData<List<AutocompletePrediction>> = MutableLiveData()
    val searchRouteResponse: MutableLiveData<List<RouteModel>> = MutableLiveData()

    var shuttleReservationDate: Date? = null

    var reservationAdded: MutableLiveData<BaseResponse> = MutableLiveData()
    var reservationCancelled: MutableLiveData<BaseResponse> = MutableLiveData()

    val shifts: MutableLiveData<List<ShiftModel>> = MutableLiveData()

    var selectedShiftIndex: Int? = null
    var selectedShift: ShiftModel? = null

    var selectedShuttleDay: ShuttleDayModel? = null
    var isShuttleDayMorning: Boolean? = null

    val allNextRides: MutableLiveData<List<ShuttleNextRide>> = MutableLiveData()
    val myNextRides: MutableLiveData<List<ShuttleNextRide>> = MutableLiveData()
    val nextRides: MutableLiveData<List<ShuttleNextRide>> = MutableLiveData()
    val calendarSelectedRides: MutableLiveData<ShuttleNextRide> = MutableLiveData()

    val vanpoolPassengers: MutableLiveData<List<PersonsModel>> = MutableLiveData()

    val routesDetails: MutableLiveData<List<RouteModel>> = MutableLiveData()
    val routesDetailsDriver: MutableLiveData<RouteModel> = MutableLiveData()

    val demandWorkgroupResponse: MutableLiveData<BaseResponse> = MutableLiveData()

    val cancelDemandWorkgroupResponse: MutableLiveData<BaseResponse> = MutableLiveData()

    val requestWorkGroupsResponse: MutableLiveData<WorkgroupResponse> = MutableLiveData()

    var currentMyRideIndex = 0

    var isComingSurvey :  Boolean = false
    var isFromAddressSelect :  Boolean = false

    var selectedFromDestination: DestinationModel? = null
    var selectedFromDestinationIndex: Int? = null

    var selectedToDestination: DestinationModel? = null
    var selectedToDestinationIndex: Int? = null

    var selectedFromLocation: FromToLocation? = null
    var selectedToLocation: FromToLocation? = null

    var dateAndWorkgroupList: List<DateAndWorkgroup>? = null

    var selectedDate: DateAndWorkgroup? = null
    var selectedDateIndex: Int? = null

    var selectedStation = AppDataManager.instance.personnelInfo?.station

    val isLocationToHome : MutableLiveData<Boolean> = MutableLiveData()
    var workgroupInstance: WorkGroupInstance? = null
    var workgroupTemplate: WorkGroupTemplate? = null
    var calendarSelectedDay: Date = Date()
    var currentRoute: RouteModel? = null
    var searchedStops = MutableLiveData<List<StationModel>?>()

    var workgroupTemplateList = MutableLiveData<List<WorkGroupTemplate>>()

    var searchedRoutes = MutableLiveData<List<RouteModel>?>()
    var searchedRoutePreview = MutableLiveData<RouteModel>()
    var selectedRoute: RouteModel? = null
    var shouldFocusCurrentLocation: Boolean = true
    var isShuttleServicePlaningFragment: Boolean = false
    var isFromCampus: Boolean = false
    var isVisibleMessage: Boolean? = null
    var fromPage: String? = ""
    var isShuttleTimeMultiple: Boolean = true
    data class FromToLocation(
            val location: Location,
            val text: String,
            val destinationId: Int?
    )

    data class DateAndWorkgroup(
            var date: Long,
            val workgroupId: Long,
            val workgroupStatus: WorkgroupStatus?,
            val fromType: FromToType,
            val fromTerminalReferenceId: Long?,
            val ride: ShuttleNextRide,
            val template: WorkGroupTemplate?
    )

    fun clearSelections() {
        selectedFromLocation = null
        selectedToLocation = null

        selectedFromDestination = null
        selectedToDestination = null

        selectedFromDestinationIndex = null
        selectedToDestinationIndex = null
    }


    fun getRouteDetailsById(routeId: Long) {
        val reservationDay = currentRide?.firstDepartureDate?.convertForDate() ?: ""

        compositeDisposable.add(
                shuttleRepository.getRouteDetails(routeId, reservationDay)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if (myRouteDetails.value == null) {
                                myRouteDetails.value = response.route
                            }
                            routeResponse.value = response
                            routeDetails.value = response.route
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun getRouteDetailsByIdSearchRoute(routeId: Long) {

        compositeDisposable.add(
                shuttleRepository.getRouteDetails(routeId, "")
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            routeSelectedForReservation.value = response.route
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

    fun getVehicleLocation(workgroupInstanceId: Long?) {

        compositeDisposable.add(
                shuttleRepository.getVehicleLocation(workgroupInstanceId!!)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if (response.error == null) {
                                vehicleLocation.value = response
                            }
                            else {
                                if (response.error?.errorId == 72) {
                                    sessionExpireError.value = true
                                } else {
                                    vehicleErrorMessage.value = response?.error?.message
                                }
                            }
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            vehicleLocation.value = null
                        }, {
                        }, {
                        }
                        )
        )
    }

    fun routeTrack() {

        compositeDisposable.add(
                shuttleRepository.routeTrack()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({
                            //vehicleLocation.value = response
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

    fun getPersonnelInfo() {
        compositeDisposable.add(
                userRepository.getPersonalDetails()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if (response.error != null) {
                                navigator?.showLoginActivity()
                            } else {
                                personnelDetailsResponse.value = response
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

    fun getShuttleUseDays() {
        compositeDisposable.add(
                shuttleRepository.getShuttleUseDays(startDate.convertForBackend(), endDate.convertForBackend())
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if (response.error != null) {
                                if (response.error?.errorId == 72) {
                                    sessionExpireError.value = true
                                } else {
                                    navigator?.handleError(Exception(response.error?.message))
                                }
                            } else {
                                getShuttleUseDaysResponse.value = response
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

    fun updateShuttleDay(shuttleModel: ShuttleDayModel) {
        compositeDisposable.add(
                shuttleRepository.updateShuttleDay(shuttleModel)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if (response.error != null) {
                                if (response.error?.errorId == 72) {
                                    sessionExpireError.value = true
                                } else {
                                    navigator?.handleError(Exception(response.error?.message))
                                    getShuttleUseDays()
                                }
                            } else {
                                updateShuttleDayResult.value = true
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

    fun getDestinations() {
        compositeDisposable.add(
                ticketRepository.getDestinations()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            destinations.value = response.response
                        }, { ex ->
                            AppLogger.e(ex, "operation failed.")
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

    fun getStops(request: RouteStopRequest, fromPlanning: Boolean = false, context: Context? = null) {
        compositeDisposable.add(
                shuttleRepository.getStops(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if (response != null) {

                                val routeIdList = mutableListOf<Long>()

                                response.forEach { station ->
                                    if (routeIdList.contains(station.routeId).not()) {
                                        routeIdList.add(station.routeId)
                                    }
                                }
                                searchedStops.value = response
                                getRoutesDetailsWith(RoutesDetailRequestModel(routeIdList))
                            }
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            setIsLoading(false)
                            val exception = if(fromPlanning){
                                Throwable(context?.getString(R.string.opt_time_over))
                            } else{
                                ex
                            }
                             navigator?.handleError(exception)
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    private fun getStopDetailsForStop(routeMap: MutableMap<Long, MutableList<StationModel>>, routeIds: MutableList<Long>, index: Int) {
        if (routeIds.size > index) {
            val routeId = routeIds[index]

            compositeDisposable.add(
                    shuttleRepository.getRouteDetails(routeId,"")
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                val stationList = routeMap[routeId]
                                stationList?.forEach { station ->
                                    station.route = response
                                }
                                getStopDetailsForStop(routeMap, routeIds, index + 1)
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
        } else {
            val stations = mutableListOf<StationModel>()
            routeMap.values.forEach {
                stations.addAll(it)
            }
            getStopsResponse.value = RouteStopResponse(
                    response = stations
            )
        }


    }

    private fun getRoutesDetailsWith(routesRequest: RoutesDetailRequestModel) {
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

    fun getStationsOfRoutes(routes: List<RouteModel>) {
        getStationsOfRoutesTail(routes, 0, mutableListOf())
    }

    private fun getStationsOfRoutesTail(routes: List<RouteModel>, index: Int, output: MutableList<StationModel>) {
        if (index >= routes.size) {

            val routeIdMap = mutableMapOf<Long, MutableList<StationModel>>()

            val routeIdList = mutableListOf<Long>()

            output.forEach { station ->
                if (routeIdMap.containsKey(station.routeId)) {
                    val stationList = routeIdMap[station.routeId]
                    stationList?.add(station)
                } else {
                    val stationList = mutableListOf<StationModel>()
                    stationList.add(station)
                    routeIdMap[station.routeId] = stationList
                    routeIdList.add(station.routeId)
                }
            }



            getStopDetailsForStop(routeIdMap, routeIdList, 0)
        } else {
            val isFirstLeg = currentRide?.workgroupDirection?.let { isFirstLeg(it, currentRide?.fromType!!) }
            compositeDisposable.add(
                    shuttleRepository.getRouteDetails(routes[index].id,"")
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                output.addAll(isFirstLeg?.let { response.route.getRoutePath(it)?.stations }
                                        ?: listOf())
                                getStationsOfRoutesTail(routes, index + 1, output)
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
    }

    fun getStopDetails(routeId: Int, request: RouteStopRequest) {
        compositeDisposable.add(
                shuttleRepository.getStopDetails(routeId, request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            stopDetailsResponse.value = response
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

    fun searchRoute(searchText: String) {
        compositeDisposable.add(
                shuttleRepository.searchRoute(searchText)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            searchRouteResponse.value = response.response
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            setIsLoading(false)
                            waitingForSearchResponse = false
                            navigator?.handleError(ex)
                        }, {
                            //setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun makeShuttleReservation(request: ShuttleReservationRequest) {
        compositeDisposable.add(
                shuttleRepository.shuttleReservation(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            if (response.error != null) {
                                navigator?.handleError(Exception(response.error?.message))
                            } else {
                                reservationAdded.value = response
                                selectedRoute = null
                                searchedRoutes.value = null
                                searchedStops.value = null
                                workgroupInstance = null
                                workgroupTemplate = null
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

    fun makeShuttleReservation2(request: ShuttleReservationRequest2, isVisibleMessage: Boolean) {
        compositeDisposable.add(
                shuttleRepository.shuttleReservation2(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            if (response.error != null) {
                                navigator?.handleError(Exception(response.error?.message))
                            } else {
                                this.isVisibleMessage = isVisibleMessage
                                reservationAdded.value = response
                                selectedRoute = null
                                searchedRoutes.value = null
                                workgroupInstance = null
                                workgroupTemplate = null

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

    fun changeShuttleSelectedDate(myRide: ShuttleNextRide, startDate: String?, endDate: String?, isRoundTrip: Boolean?){
        val useFirstLeg = if (myRide.firstLeg) (myRide.notUsing) else null
        val useReturnLeg = if (!myRide.firstLeg) (myRide.notUsing) else null
        val reservationDay = Date(myRide.firstDepartureDate).convertForBackend2()

        val shuttleReservationRequest = ShuttleReservationRequest2(reservationDay= reservationDay, reservationDayEnd= endDate,
                workgroupInstanceId= myRide.workgroupInstanceId, routeId= myRide.routeId ?: 0, useFirstLeg= useFirstLeg, firstLegStationId= null,
                useReturnLeg= useReturnLeg, returnLegStationId= null)

        if(isRoundTrip == true){
            val usage = myRide.notUsing
            shuttleReservationRequest.useFirstLeg = usage
            shuttleReservationRequest.useReturnLeg = usage
            shuttleReservationRequest.reservationDay = startDate ?: reservationDay
            shuttleReservationRequest.reservationDayEnd = endDate
        }

        cancelShuttleReservation2(shuttleReservationRequest)

    }


    fun cancelShuttleReservation2(request: ShuttleReservationRequest2) {
        compositeDisposable.add(
                shuttleRepository.shuttleReservation2(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            if (response.error != null) {
                                navigator?.handleError(Exception(response.error?.message))
                            } else {
                                reservationCancelled.value = response
                                selectedRoute = null
                                searchedRoutes.value = null
                                workgroupInstance = null
                                workgroupTemplate = null
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

    fun cancelShuttleReservation(request: ShuttleReservationCancelRequest) {
        compositeDisposable.add(
                shuttleRepository.cancelShuttleReservation(request)
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

    fun getShifts() {

        val campusPlace = (if (fromPlace.value?.isCampus == true) {
            fromPlace.value
        } else {
            toPlace.value
        })
                ?: return

        campusPlace.id?.let { destinationId ->
            compositeDisposable.add(
                    shuttleRepository.getShifts(destinationId)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->

                                if (response.error != null) {
                                    navigator?.handleError(Exception(response.error?.message))
                                } else {
                                    shifts.value = response.response
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
    }

    fun getRouteDetailsForAttendanceReservation(routeId: Long) {
        val reservationDay = currentRide?.firstDepartureDate?.convertForDate() ?: ""
        compositeDisposable.add(
                shuttleRepository.getRouteDetails(routeId,reservationDay)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            routeDetailsForAttendenceReservation.value = response.route
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

    fun getAllNextRides() {

        compositeDisposable.add(
                shuttleRepository.getAllNextRides()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            allNextRides.value = response
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

    fun getRoutesDetails(routeIds: Set<Long>) {

        compositeDisposable.add(
                shuttleRepository.getRoutesDetails(routeIds)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            routesDetails.value = response.response
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

    fun getReservationRequestModel(stop: StationModel) : ShuttleReservationRequest2? {
        if (workgroupInstance != null) {
            workgroupInstance?.let { instance ->
                workgroupTemplate?.let { template ->
                    val useFirstLeg = true
                    val useReturnLeg = (template.direction == WorkgroupDirection.ROUND_TRIP) ?: null
                    return ShuttleReservationRequest2(
                        reservationDay = calendarSelectedDay.convertForBackend2(),
                        reservationDayEnd= null,
                        workgroupInstanceId = instance.id,
                        routeId = selectedRoute?.id?:0L,
                        useFirstLeg = useFirstLeg,
                        firstLegStationId = if (useFirstLeg) selectedStation?.id else null,
                        useReturnLeg = useReturnLeg,
                        returnLegStationId = if (useReturnLeg == true) stop.id else null
                    )
                }

            }
        } else {
            selectedDate?.let {
                it.ride.let { selectedRide ->
                    val firstLeg = selectedRide.firstLeg
                    return ShuttleReservationRequest2(
                        reservationDay = Date(selectedRide.firstDepartureDate).convertForBackend2(),
                        reservationDayEnd =  null,
                        workgroupInstanceId = selectedRide.workgroupInstanceId,
                        routeId = selectedRoute?.id?:0L,
                        useFirstLeg = true,
                        firstLegStationId = if (firstLeg) stop.id else null,
                        useReturnLeg = if (firstLeg.not()) true else null,
                        returnLegStationId = if (firstLeg.not()) stop.id else null
                    )
                }
            }
        }
        return  null
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

    fun cancelDemandWorkgroup(request: WorkgroupDemandRequest) {

        compositeDisposable.add(
                shuttleRepository.cancelDemandWorkgroup(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            cancelDemandWorkgroupResponse.value = response
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

    fun requestWorkGroups() {

        compositeDisposable.add(
                shuttleRepository.requestWorkGroups()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            requestWorkGroupsResponse.value = response
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
    fun getWorkgroupInformation() {

        compositeDisposable.add(
                shuttleRepository.getWorkgroupInformation(AppDataManager.instance.personnelInfo?.workgroupInstanceId ?: 0)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            routeForWorkgroup.value = response
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
                shuttleRepository.getWorkgroupInformation(workgroupInstanceId ?: 0)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            routeForWorkgroup.value = response
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            setIsLoading(false)
//                            navigator?.handleError(ex)
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }


    fun getWorkgroupInformationWithId(workgroupInstanceId: Long) {

        compositeDisposable.add(
                shuttleRepository.getWorkgroupInformation(workgroupInstanceId)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                          //  routeForWorkgroup.value = response
                            workgroupInstance = response.instance
                            isReturningShuttlePlanningEdit = false
                            openBottomSheetEditShuttle.value = true
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

    fun getWorkgroupNearbyStationRequest() {

        compositeDisposable.add(
                shuttleRepository.getWorkgroupNearbyStationRequest(AppDataManager.instance.personnelInfo?.workgroupInstanceId ?: 0)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            hasNearbyRequest.value = response.response
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
    fun cancelWorkgroupNearbyStationRequest() {

        compositeDisposable.add(
                shuttleRepository.cancelWorkgroupNearbyStationRequest(AppDataManager.instance.personnelInfo?.workgroupInstanceId ?: 0)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({
                            successNearbyRequest.value = true
                        }, {
                            setIsLoading(false)
                            successNearbyRequest.value = true
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun createWorkgroupNearbyStationRequest() {

        compositeDisposable.add(
                shuttleRepository.createWorkgroupNearbyStationRequest(AppDataManager.instance.personnelInfo?.workgroupInstanceId ?: 0)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({
                            successNearbyRequest.value = true
                        }, {
                            setIsLoading(false)
                            successNearbyRequest.value = true
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun isFirstLeg(direction: WorkgroupDirection, fromType: FromToType) : Boolean {
        if (direction == WorkgroupDirection.ONE_WAY)
            return true

        return  fromType == FromToType.PERSONNEL_SHUTTLE_STOP || fromType == FromToType.PERSONNEL_HOME_ADDRESS
    }

    fun getTemplateForInstance(workgroupInstance: WorkGroupInstance): WorkGroupTemplate? {
        requestWorkGroupsResponse.value?.templates?.let { templates ->
            for (template in templates) {
                if (template.id == workgroupInstance.templateId) {
                    return  template
                }
            }
        }

        return null
    }

    enum class SearchType {
        from,
        to
    }

    enum class SelectType {
        CampusFrom,
        CampusTo,
        Time,
        RouteSorting
    }

    var currentRide : ShuttleNextRide? = null

    var isFromChanged = false
    var isToChanged = false

    val openNumberPicker: MutableLiveData<SelectType> = MutableLiveData()
    val setSearchListAdapter: MutableLiveData<Boolean> = MutableLiveData()
    val workgroupType: MutableLiveData<String> = MutableLiveData()

    val cardCurrentRide: MutableLiveData<ShuttleNextRide> = MutableLiveData()
    val stations: MutableLiveData<List<StationModel>?> = MutableLiveData()

    val textViewBottomSheetEditShuttleRouteName: MutableLiveData<String> = MutableLiveData()
    val textViewBottomSheetEditShuttleRouteFrom: MutableLiveData<String> = MutableLiveData()
    val textViewBottomSheetEditShuttleRouteTo: MutableLiveData<String> = MutableLiveData()
    val switchBottomSheetEditShuttleUse: MutableLiveData<Boolean> = MutableLiveData()
    val editTextAddressSearch: MutableLiveData<String> = MutableLiveData()
    val viewBackgroundBottomSheetVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    val imageViewFromToRes: MutableLiveData<Int> = MutableLiveData()
    val textViewFromToTextColor: MutableLiveData<Int> = MutableLiveData()
    val textViewFromToText: MutableLiveData<String> = MutableLiveData()
    val textViewBottomSheetEditShuttleRouteTime: MutableLiveData<String> = MutableLiveData()
    val bottomSheetBehaviorEditShuttleState: MutableLiveData<Int> = MutableLiveData()
    val bottomSheetVisibility: MutableLiveData<Boolean> = MutableLiveData()
    val editClicked: MutableLiveData<Boolean> = MutableLiveData()
    val noRoutesForEdit: MutableLiveData<Boolean> = MutableLiveData()

    var fromLocation: SearchRequestModel? = null
    var toLocation: SearchRequestModel? = null

    var isFromToShown = false
    var isFrom = false

    var isReturningShuttleEdit = false
    var isReturningShuttlePlanningEdit = false
    var isMakeReservationOpening = false

    var zoomStation = false

    val openBottomSheetCalendar: MutableLiveData<Boolean> = MutableLiveData()
    val openBottomSheetEditShuttle: MutableLiveData<Boolean> = MutableLiveData()
    val openBottomSheetFromWhere: MutableLiveData<Boolean> = MutableLiveData()
    val openBottomSheetSearchRoute: MutableLiveData<Boolean> = MutableLiveData()
    val openBottomSheetRoutes: MutableLiveData<Boolean> = MutableLiveData()
    val openBottomSheetRoutePreview: MutableLiveData<Boolean> = MutableLiveData()
    val openBottomSheetMakeReservation: MutableLiveData<Boolean> = MutableLiveData()
    val openRouteSelection: MutableLiveData<Boolean> = MutableLiveData()
    val openStopSelection: MutableLiveData<Boolean> = MutableLiveData()
    val openBottomSheetSelectRoutes: MutableLiveData<Boolean> = MutableLiveData()
    val openVanpoolDriverStations: MutableLiveData<Boolean> = MutableLiveData()
    val openVanpoolPassenger: MutableLiveData<Boolean> = MutableLiveData()
    val openReservationView: MutableLiveData<Boolean> = MutableLiveData()

    var selectedStopForReservation: StationModel? = null

    val textViewBottomSheetStopName: MutableLiveData<String> = MutableLiveData()
    val textViewBottomSheetVehicleName: MutableLiveData<String> = MutableLiveData()
    val textViewBottomSheetReservationDate: MutableLiveData<String> = MutableLiveData()
    val textViewBottomSheetRoutesFromToName: MutableLiveData<String> = MutableLiveData()
    val textViewBottomSheetRoutesTitle: MutableLiveData<String> = MutableLiveData()
    val textviewFullnessValue: MutableLiveData<String> = MutableLiveData()
    val textViewDurationWalking: MutableLiveData<String> = MutableLiveData()

    val fillUITrigger: MutableLiveData<RouteModel> = MutableLiveData()
    val navigateToMapTrigger: MutableLiveData<Boolean> = MutableLiveData()
    val searchRoutesAdapterSetMyDestinationTrigger : MutableLiveData<DestinationModel> = MutableLiveData()
    val searchRoutesAdapterSetListTrigger: MutableLiveData<MutableList<RouteModel>> = MutableLiveData()
    val selectedRouteSortItemIndexTrigger: MutableLiveData<Int> = MutableLiveData()
    val routeForWorkgroup: MutableLiveData<WorkgroupResponse> = MutableLiveData()
    val hasNearbyRequest: MutableLiveData<Boolean> = MutableLiveData()
    val successNearbyRequest: MutableLiveData<Boolean> = MutableLiveData()

    val routeSortList = listOf(RouteSortType.WalkingDistance, RouteSortType.TripDuration, RouteSortType.OccupancyRatio)

    var selectedRouteSortItemIndex : Int? = null

    enum class RouteSortType {
        WalkingDistance,
        TripDuration,
        OccupancyRatio
    }

}