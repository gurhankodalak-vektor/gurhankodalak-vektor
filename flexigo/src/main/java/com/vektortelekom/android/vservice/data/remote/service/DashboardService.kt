package com.vektortelekom.android.vservice.data.remote.service

import com.vektortelekom.android.vservice.data.model.DashboardResponse
import com.vektortelekom.android.vservice.data.model.NotificationsResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface DashboardService {

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/dashboard")
    fun getDashboard(@Query("langCode") langCode: String): Observable<DashboardResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/notification/list")
    fun getNotifications(): Observable<NotificationsResponse>

}