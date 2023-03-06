package com.vektortelekom.android.vservice.data.repository

import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.remote.service.RouteService
import javax.inject.Inject

class ShuttleRepository
@Inject
constructor(
        private val routeService: RouteService
) {

    fun getRouteDetails(routeId: Long, reservationDay: String) = routeService.getRouteDetails(routeId, reservationDay)

    fun getVehicleLocation(workgroupInstanceId: Long) = routeService.getVehicleLocation(workgroupInstanceId)

    fun routeTrack() = routeService.routeTrack()

    fun myCampus() = routeService.myCampus()

    fun getShuttleUseDays(startDay: String, endDay: String) = routeService.getShuttleUseDays(startDay, endDay)

    fun updateShuttleDay(shuttleDay: ShuttleDayModel) = routeService.updateShuttleDay(shuttleDay)

    fun readQrCode(routeQrCode: String, latitude: Double, longitude: Double) = routeService.readQrCode(ReadQrCodeRequest(routeQrCode, latitude, longitude))

    fun getStops(request: RouteStopRequest) = routeService.getStops(request)

    fun getStopDetails(routeId: Int, request: RouteStopRequest) = routeService.getStopDetails(routeId, request)

    fun updatePersonnelStation(id: Long) = routeService.updatePersonnelStation(UpdatePersonnelStationRequest(id))

    fun searchRoute(searchText: String) = routeService.searchRoute(SearchRouteRequest(searchText))

    fun shuttleReservation(request: ShuttleReservationRequest) = routeService.shuttleReservation(request)

    fun cancelShuttleReservation(request: ShuttleReservationCancelRequest) = routeService.shuttleCancelReservation(request)

    fun getShifts(destinationId: Long) = routeService.getShifts(GetShiftRequest(destinationId))

    fun getAllNextRides() = routeService.getAllNextRides()

    fun getAllNextRidesWithLocation(latitude: Double, longitude: Double) = routeService.getAllNextRides(latitude, longitude)

    fun getMyNextRides() = routeService.getMyNextRides()

    fun getRoutesDetails(routeIds : Set<Long>) = routeService.getRoutesDetails(RoutesDetailsModel(routeIds = routeIds))

    fun getRoutesDetailsWith(request: RoutesDetailRequestModel) = routeService.getRoutesWith(request)

    fun shuttleReservation2(request: ShuttleReservationRequest2) = routeService.shuttleReservation2(request)

    fun shuttleReservation3(request: ShuttleReservationRequest3) = routeService.shuttleReservation3(request)

    fun demandWorkgroup(request: WorkgroupDemandRequest) = routeService.demandWorkgroup(request)

    fun cancelDemandWorkgroup(request: WorkgroupDemandRequest) = routeService.cancelDemandWorkgroup(request)

    fun requestWorkGroups() = routeService.requestWorkGroups()

    fun getWorkgroupInformation(instanceId: Long) = routeService.getWorkgroupInformation(instanceId)

    fun getWorkgroupNearbyStationRequest(instanceId: Long) = routeService.getWorkgroupNearbyStationRequest(instanceId)

    fun cancelWorkgroupNearbyStationRequest(instanceId: Long) = routeService.cancelWorkgroupNearbyStationRequest(instanceId)

    fun createWorkgroupNearbyStationRequest(instanceId: Long) = routeService.createWorkgroupNearbyStationRequest(instanceId)

    fun getActiveRide() = routeService.getActiveRide()

    fun cancelRouteReservations(request: CancelRouteReservationsRequest) = routeService.cancelShuttleRouteReservations(request)

}