package com.vektortelekom.android.vservice.data.remote.service

import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektortelekom.android.vservice.data.model.*
import io.reactivex.Observable
import retrofit2.http.*

interface TicketService {

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/ticket/list")
    fun getTickets(@Query("langCode") langCode: String): Observable<GetTicketsResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/ticket/types")
    fun getTicketTypes(): Observable<GetTicketTypesResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/destinations?routeCategory=NORMAL")
    fun getDestinations(): Observable<GetDestinationsResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/routes")
    fun getDestinationRoutes(@Body request: GetDestinationRoutesRequest): Observable<GetDestinationRoutesResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/ticket/new")
    fun createTicket(@Body request: CreateTicketRequest): Observable<BaseResponse>

}