package com.vektortelekom.android.vservice.data.remote.service

import com.google.gson.JsonObject
import com.vektortelekom.android.vservice.data.model.SendGoogleAuthCodeRequest
import io.reactivex.Observable
import retrofit2.http.*

interface CalendarService {

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/calendar/authcode")
    fun sendGoogleAuthCode(@Body request: SendGoogleAuthCodeRequest): Observable<JsonObject>

}