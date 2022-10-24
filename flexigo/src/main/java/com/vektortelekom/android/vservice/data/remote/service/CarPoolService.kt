package com.vektortelekom.android.vservice.data.remote.service

import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektortelekom.android.vservice.data.model.*
import io.reactivex.Observable
import retrofit2.http.*

interface CarPoolService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/carpool")
    fun getCarpool(): Observable<CarPoolResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/carpool/driver/choose")
    fun setChooseRider(@Body riderRequest: ChooseRiderRequest): Observable<BaseResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/carpool/rider/choose")
    fun setChooseDriver(@Body driverRequest: ChooseDriverRequest): Observable<BaseResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/carpool/preferences")
    fun updateCarpoolPreferences(@Body carPoolPreferencesRequest: CarPoolPreferencesRequest): Observable<BaseResponse>

}
