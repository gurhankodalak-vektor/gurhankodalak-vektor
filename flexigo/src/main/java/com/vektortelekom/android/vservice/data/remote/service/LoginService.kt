package com.vektortelekom.android.vservice.data.remote.service

import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektortelekom.android.vservice.data.model.*
import io.reactivex.Observable
import retrofit2.http.*
import java.util.HashMap

interface LoginService {

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/personnel/info")
    fun getPersonalDetails(): Observable<PersonelInfoResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/oauth/personnel/mobile/login/v2")
    fun login(@Body loginRequest: LoginRequest): Observable<LoginResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/oauth/web/forgotPassword")
    fun forgotPassword(@Body forgotPasswordRequest: ForgotPasswordRequest): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/device/update")
    fun updateFirebaseToken(@Body updateFirebaseTokenRequest: UpdateFirebaseTokenRequest): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/pool/public/mobileParameters")
    fun getMobileParameters(): Observable<HashMap<String, Any>>


}