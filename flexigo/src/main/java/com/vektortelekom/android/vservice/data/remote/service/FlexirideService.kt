package com.vektortelekom.android.vservice.data.remote.service

import com.vektortelekom.android.vservice.data.model.PoolcarAndFlexirideModel
import io.reactivex.Observable
import org.json.JSONObject
import retrofit2.http.*

interface FlexirideService {

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/flexiride/requests")
    fun createFlexiride(@Body request: PoolcarAndFlexirideModel): Observable<PoolcarAndFlexirideModel>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/flexiride/requests")
    fun getFlexirideList(): Observable<List<PoolcarAndFlexirideModel>>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name_2}/rest/flexiride/requests/{id}")
    fun getFlexiride(@Path(value = "id") id: Int): Observable<PoolcarAndFlexirideModel>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name_2}/rest/flexiride/requests/{id}/cancel")
    fun deleteFlexiride(@Path(value = "id") id: Int, @Body body: String = ""): Observable<JSONObject>

}