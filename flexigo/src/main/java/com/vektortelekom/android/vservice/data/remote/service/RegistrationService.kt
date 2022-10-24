package com.vektortelekom.android.vservice.data.remote.service

import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektortelekom.android.vservice.data.model.*
import io.reactivex.Observable
import retrofit2.http.*
import java.util.HashMap

interface RegistrationService {

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/register/personnel/check-domain")
    fun checkDomain(@Body checkDomainRequest: CheckDomainRequest, @Query("langCode") langCode: String): Observable<CheckDomainResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/register/personnel/verify-company-auth-code")
    fun sendCompanyCode(@Body registerVerifyCompanyCodeRequest: RegisterVerifyCompanyCodeRequest, @Query("langCode") langCode: String): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/register/personnel/verify-email")
    fun verifyEmail(@Body emailVerifyEmailRequest: EmailVerifyEmailRequest, @Query("langCode") langCode: String): Observable<VerifyEmailResponse>

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
    @POST("/{app_name}/rest/mobile/personnel/destination/update")
    fun destinationsUpdate(@Body request: UpdatePersonnelCampusRequest): Observable<PersonelInfoResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("/pool/public/mobileParameters")
    fun getMobileParameters(): Observable<HashMap<String, Any>>

}
