package com.vektortelekom.android.vservice.data.repository

import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.remote.service.RegistrationService
import io.reactivex.Observable
import javax.inject.Inject

class RegistrationRepository
@Inject
constructor(
        private val registrationService: RegistrationService
) {

        fun checkDomain(checkDomainRequest: CheckDomainRequest, langCode: String) : Observable<CheckDomainResponse> {
                return registrationService.checkDomain(checkDomainRequest, langCode)
        }

        fun sendCompanyCode(registerVerifyCompanyCodeRequest: RegisterVerifyCompanyCodeRequest, langCode: String) : Observable<BaseResponse> {
                return registrationService.sendCompanyCode(registerVerifyCompanyCodeRequest, langCode)
        }

        fun verifyEmail(emailVerifyEmailRequest: EmailVerifyEmailRequest, langCode: String) : Observable<VerifyEmailResponse> {
                return registrationService.verifyEmail(emailVerifyEmailRequest, langCode)
        }

        fun getDestinations() = registrationService.getDestinations()

        fun destinationsUpdate(request: UpdatePersonnelCampusRequest) = registrationService.destinationsUpdate(request)
}