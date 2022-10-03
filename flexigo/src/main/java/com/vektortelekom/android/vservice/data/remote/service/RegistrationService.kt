package com.vektortelekom.android.vservice.data.remote.service

import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektor.vshare_api_ktx.model.MultiRatingRequest
import com.vektor.vshare_api_ktx.model.UploadResponse2
import com.vektortelekom.android.vservice.data.model.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.*
import retrofit2.http.Path

interface RegistrationService {

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/personnel/info/update")
    fun infoUpdate(@Body infoUpdateRequest: InfoUpdateRequest): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/personnel/password/update")
    fun changePassword(@Body changePasswordRequest: ChangePasswordRequest): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/personnel/home/update")
    fun updateUser(@Body latLng: FlexigoLatLng): Observable<PersonelInfoResponse>

    @Multipart
    @POST("/{app_name}/rest/mobile/file/uploadMany")
    fun uploadImages2(@Part files: List<MultipartBody.Part>): Observable<UploadResponse2>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/personnel/info/update")
    fun changeProfilePhoto(@Body changeProfilePhotoRequest: ChangeProfilePhotoRequest): Observable<BaseResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/enums/{categoryName}")
    fun getEnums(@Path("categoryName") categoryName: String, @Query("langCode") langCode: String): Observable<VektorEnumResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/rate")
    fun sendMultiRating(@Body request: MultiRatingRequest): Observable<BaseResponse>

}
