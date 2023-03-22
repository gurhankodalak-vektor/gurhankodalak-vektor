package com.vektortelekom.android.vservice.data.remote.service

import com.google.gson.JsonObject
import com.vektor.vshare_api_ktx.model.*
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.model.CustomerStatusModel
import com.vektortelekom.android.vservice.data.model.PriceModel2
import io.reactivex.Observable
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.http.*
import retrofit2.http.Path
import java.util.HashMap

interface PoolCarService {

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/parks")
    fun getStations(): Observable<List<ParkModel>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/parks/{id}/catalogs")
    fun getStationVehicles(@Path(value = "id") id: Int): Observable<List<StationVehicleModel>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/customer/status")
    fun getCustomerStatus(): Observable<CustomerStatusModel>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/rental/create")
    fun createRental(@Body request: RentalCreateRequest2): Observable<CreateRentalResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/rental/end")
    fun cancelRental(@Body request: RentalEndRequest): Observable<CreateRentalResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/rental/start2")
    fun startRental(@Body request: StartRentalRequest): Observable<CreateRentalResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/vehicle/damages")
    fun getVehicleDamages(@Query("vehicleId") vehicleId: Int): Observable<List<DamageModel>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/rental/damages")
    fun getRentalDamages(@Query("rentalId") rentalId: Int): Observable<List<DamageModel>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/rental/end")
    fun rentalEnd(@Body request: RentalEndRequest): Observable<CreateRentalResponse>

    @Multipart
    @POST("/report/rest/file/uploadMany2")
    fun uploadImages2(@Part files: List<MultipartBody.Part>): Observable<UploadResponse2>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/rental/addDamage")
    fun addCarDamages(@Body damageRequest: DamageRequest): Observable<JSONObject>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/rental/checkDoor")
    fun checkDoorStatus(@Query("rentalId") rentalId: Int): Observable<CheckDoorResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/public/mobileParameters")
    fun getMobileParameters(): Observable<HashMap<String, Any>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/poolcar/requests")
    fun getReservations(): Observable<List<PoolcarAndFlexirideModel>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/poolcar/requests/reasons ")
    fun getReservationReasons(): Observable<List<String>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/poolcar/requests")
    fun addReservation(@Body request: PoolcarAndFlexirideModel): Observable<PoolcarAndFlexirideModel>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/poolcar/requests/check")
    fun checkReservation(@Body request: PoolcarAndFlexirideModel): Observable<List<CheckReservationResponse>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/poolcar/requests/{id}/cancel")
    fun cancelReservation(@Path(value = "id") id: Int, @Body body: String = ""): Observable<JsonObject>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/public/poi/inRadius")
    fun getPoiList(@Body poiRequest: PoiRequest): Observable<List<PoiResponse>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/rental/billInfo")
    fun getRentalBillInfo(@Query("rentalId") rentalId: Long): Observable<BillInfoModel>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/poolcar/availablePriceModels")
    fun availablePriceModels(@Body request: PoolcarAndFlexirideModel): Observable<List<PriceModel2>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/poolcar/poi-list")
    fun getPoiList(): Observable<List<PoiModel>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/poolcar/requests/{id}/start")
    fun startIntercityRental(@Path(value = "id") id: Int, @Body request: PoolCarIntercityStartRequest): Observable<PoolcarAndFlexirideModel>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/poolcar/requests/{id}/end")
    fun finishIntercityRental(@Path(value = "id") id: Int, @Body request: PoolCarIntercityFinishRequest): Observable<PoolcarAndFlexirideModel>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/flexiride/requests/{requestId}/updateVehicle")
    fun updateReservationVehicleWithQr(@Path(value = "requestId") id: Int, @Body request: UpdateReservationVehicleRequest): Observable<PoolcarAndFlexirideModel>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/poolcar/requests/{requestId}/start/precheck")
    fun reservationStartPrecheck(@Path(value = "requestId") id: Int): Observable<ResultModel>

}