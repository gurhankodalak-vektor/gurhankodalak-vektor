package com.vektortelekom.android.vservice.data.repository

import com.google.android.gms.maps.model.LatLng
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektor.ktx.data.remote.usermanagement.oauth.OAuthService
import com.vektor.vshare_api_ktx.model.DocumentRequest
import com.vektor.vshare_api_ktx.model.MultiRatingRequest
import com.vektor.vshare_api_ktx.service.DocumentService
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.remote.service.DashboardService
import com.vektortelekom.android.vservice.data.remote.service.LoginService
import com.vektortelekom.android.vservice.data.remote.service.UserService
import io.reactivex.Observable
import okhttp3.MultipartBody
import javax.inject.Inject

class UserRepository
@Inject
constructor(
    private val loginService: LoginService,
    private val dashboardService: DashboardService,
    private val userService: UserService,
    private val documentService: DocumentService,
    private val oAuthService: OAuthService
) {

    fun getPersonalDetails() = loginService.getPersonalDetails()

    fun getMobileParameters() = loginService.getMobileParameters()

    fun login(email: String, password: String, firebaseToken: String, langCode: String) : Observable<LoginResponse> {

        val request = LoginRequest(email, password, firebaseToken, "android", langCode)

        return loginService.login(request)
    }

    fun forgotPassword(email: String): Observable<BaseResponse> {

        val request = ForgotPasswordRequest(email)

        return loginService.forgotPassword(request)
    }

    fun getDashboard(langCode: String?) = dashboardService.getDashboard(langCode?:"tr")

    fun getNotifications() = dashboardService.getNotifications()

    fun updateFirebaseToken(firebaseToken: String) = loginService.updateFirebaseToken(UpdateFirebaseTokenRequest(firebaseToken))

    fun infoUpdate(infoUpdateRequest: InfoUpdateRequest) = userService.infoUpdate(infoUpdateRequest)

    fun changePassword(changePasswordRequest: ChangePasswordRequest) = userService.changePassword(changePasswordRequest)

    fun updateUser(latLng: LatLng) = userService.updateUser(FlexigoLatLng(latLng.latitude, latLng.longitude))

    fun downloadFileWithDynamicUrlSync(fileUrl: String) = documentService.downloadFileWithDynamicUrlSync(fileUrl)

    fun uploadImages(files: List<MultipartBody.Part>) = userService.uploadImages2(files)

    fun changeProfilePhoto(photoUuid: String) = userService.changeProfilePhoto(ChangeProfilePhotoRequest((photoUuid)))

    fun getPurposesOfTaxiUse(langCode: String?) = userService.getEnums("purposeOfTaxiUse", langCode?:"tr")

    fun sendMultiRating(request: MultiRatingRequest) = userService.sendMultiRating(request)

    fun addDocument(request: DocumentRequest) = documentService.addDocument(request)

    fun getLatestDrivingLicence(accountId: String) = documentService.latestDrivingLicence(accountId)

    fun logout() = userService.logout()

    fun logoutUmng() = oAuthService.logout()

    fun searchPersonWithRegistrationNumber(registrationNumber: String) = userService.searchPersonWithRegistrationNumber(SearchPersonRequest(registrationNumber))

    fun agreeKvkk(request: AgreeKvkkRequest) = userService.agreeKvkk(request)

    fun getVanpoolApprovalList() = userService.getVanpoolApprovalList()

    fun getWorkgroupInformation(instanceId: Long) = userService.getWorkgroupInformation(instanceId)

    fun getDraftRouteDetails(routeId: Long) = userService.getDraftRouteDetails(routeId)

    fun getCampusInfo(campusId: Long) = userService.getCampusInfo(campusId)

    fun updateResponse(approvalItemId: Long, responseType: String) = userService.updateResponse(approvalItemId, responseType)

    fun getCarpool() = userService.getCarpool()

    fun readQrCodeShuttle(routeQrCode: String, latitude: Double, longitude: Double) = userService.readQrCodeShuttle(ReadQrCodeRequest(routeQrCode, latitude, longitude))

    fun sendQrCode(value: ResponseModel) = userService.sendQrCode(value)

}