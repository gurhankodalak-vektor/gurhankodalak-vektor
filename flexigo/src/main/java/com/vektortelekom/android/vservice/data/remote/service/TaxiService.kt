package com.vektortelekom.android.vservice.data.remote.service

import com.vektortelekom.android.vservice.data.model.CreateTaxiResponse
import com.vektortelekom.android.vservice.data.model.CreateTaxiUsageRequest
import com.vektortelekom.android.vservice.data.model.TaxiListResponse
import io.reactivex.Observable
import retrofit2.http.*

interface TaxiService {

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/taxi/use/new")
    fun createTaxiUsage(@Body request: CreateTaxiUsageRequest): Observable<CreateTaxiResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/taxi/use/{id}/update")
    fun updateTaxiUsage(@Path("id") id: Int, @Body request: CreateTaxiUsageRequest): Observable<CreateTaxiUsageRequest>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/taxi/use/list")
    fun getTaxiUsages(): Observable<TaxiListResponse>

}