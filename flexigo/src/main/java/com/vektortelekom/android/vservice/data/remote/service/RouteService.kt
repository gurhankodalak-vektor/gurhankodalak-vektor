package com.vektortelekom.android.vservice.data.remote.service

import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.model.workgroup.WorkgroupResponse
import io.reactivex.Observable
import retrofit2.http.*
import retrofit2.http.Path

interface RouteService {

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/v3/mobile/routes/{routeId}/details")
    fun getRouteDetails(@Path("routeId") routeId: Long, @Query("reservationDay") reservationDay: String): Observable<RouteResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/vehicle/latest")
    fun getVehicleLocation(): Observable<VehicleLocationResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/personnel/route/track")
    fun routeTrack(): Observable<PathModel>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/personnel/shuttleUseDay/list")
    fun getShuttleUseDays(@Query("startDay") startDay: String,  @Query("endDay") endDay: String): Observable<GetShuttleUseDaysResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/personnel/shuttleUseDay/update")
    fun updateShuttleDay(@Body shuttleDay: ShuttleDayModel): Observable<BaseResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/routes/details")
    fun getRoutesWith(@Body shuttleDay: RoutesDetailRequestModel): Observable<RoutesDetailResponseModel>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/routeInstance/cardEvent")
    fun readQrCode(@Body request: ReadQrCodeRequest): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json",
            "ehm: ehm"
    )
    @POST("/{app_name}/rest/v3/mobile/search-stations")
    fun getStops(@Body request: RouteStopRequest): Observable<List<StationModel>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/station/search/{routeId}/details")
    fun getStopDetails(@Path("routeId") routeId: Int, @Body request: RouteStopRequest): Observable<RouteDetailResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/personnel/station/update")
    fun updatePersonnelStation(@Body request: UpdatePersonnelStationRequest): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/v3/workgroups/instance/{instanceId}/info")
    fun getWorkgroupInformation(@Path("instanceId") instanceId: Long): Observable<WorkgroupResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/routes")
    fun searchRoute(@Body request: SearchRouteRequest): Observable<SearchRouteResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/booking/station")
    fun shuttleReservation(@Body request: ShuttleReservationRequest): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/booking/station/cancel")
    fun shuttleCancelReservation(@Body request: ShuttleReservationCancelRequest): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/shift/list")
    fun getShifts(@Body request: GetShiftRequest): Observable<GetShiftsResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/v3/personnel/all-next-rides")
    fun getAllNextRides(): Observable<List<ShuttleNextRide>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/v3/personnel/my-next-rides")
    fun getMyNextRides(): Observable<List<ShuttleNextRide>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/routes/details")
    fun getRoutesDetails(@Body request : RoutesDetailsModel): Observable<RoutesDetails>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/v3/personnel/reservations")
    fun shuttleReservation2(@Body request: ShuttleReservationRequest2): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/v3/personnel/reservations")
    fun shuttleReservation3(@Body request: ShuttleReservationRequest3): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/v3/personnel/workgroups/demand")
    fun demandWorkgroup(@Body request: WorkgroupDemandRequest): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/v3/personnel/workgroups/cancel-demand")
    fun cancelDemandWorkgroup(@Body request: WorkgroupDemandRequest): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/v3/workgroups")
    fun requestWorkGroups(): Observable<WorkgroupResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/v3/mobile/workgroup-instances/{instanceId}/station-request")
    fun getWorkgroupNearbyStationRequest(@Path("instanceId") instanceId: Long): Observable<GetNearbyRequestModel>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @DELETE("/{app_name}/rest/v3/mobile/workgroup-instances/{instanceId}/station-request")
    fun cancelWorkgroupNearbyStationRequest(@Path("instanceId") instanceId: Long): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/v3/mobile/workgroup-instances/{instanceId}/station-request")
    fun createWorkgroupNearbyStationRequest(@Path("instanceId") instanceId: Long): Observable<BaseResponse>


}