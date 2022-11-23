package com.vektortelekom.android.vservice.ui.registration

import androidx.lifecycle.MutableLiveData
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.repository.RegistrationRepository
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import javax.inject.Inject

class RegistrationViewModel
@Inject
constructor(private val registrationRepository: RegistrationRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<BaseNavigator>() {


    var userName: String = ""
    var userSurname: String = ""
    var userEmail: String = ""
    val companyAuthCode: MutableLiveData<String> = MutableLiveData()
    var userPassword: String = ""

    val destinations: MutableLiveData<List<DestinationModel>> = MutableLiveData()

    val isVerifySuccess: MutableLiveData<Boolean> = MutableLiveData()
    val isCompanyCodeSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val isCampusUpdateSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val sessionId: MutableLiveData<String> = MutableLiveData()
    val surveyQuestionId: MutableLiveData<Long> = MutableLiveData()

    val destinationId: MutableLiveData<Long> = MutableLiveData()
    val verifyEmailResponse: MutableLiveData<VerifyEmailResponse> = MutableLiveData()

    val isCompanyAuthCodeRequired: MutableLiveData<Boolean> = MutableLiveData()

    fun checkDomain(checkDomainRequest: CheckDomainRequest, langCode: String) {

            compositeDisposable.add(
                registrationRepository.checkDomain(checkDomainRequest, langCode)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({ response ->
                        if(response.error != null) {
                            navigator?.handleError(Exception(response.error?.message))
                        }
                        else {
                            isCompanyAuthCodeRequired.value = response.isCompanyAuthCodeRequired
                        }
                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        navigator?.handleError(ex)
                    }, {
                        setIsLoading(false)
                    }, {
                        setIsLoading(true)
                    }
                    )
            )

    }

    fun sendCompanyCode(registerVerifyCompanyCodeRequest: RegisterVerifyCompanyCodeRequest, langCode: String) {

            compositeDisposable.add(
                registrationRepository.sendCompanyCode(registerVerifyCompanyCodeRequest, langCode)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({ response ->
                        if(response.error != null)
                            navigator?.handleError(Exception(response.error?.message))
                        else
                            isCompanyCodeSuccess.value = true

                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        navigator?.handleError(ex)
                    }, {
                        setIsLoading(false)
                    }, {
                        setIsLoading(true)
                    }
                    )
            )

    }

    fun verifyEmail(emailVerifyEmailRequest: EmailVerifyEmailRequest, langCode: String) {

            compositeDisposable.add(
                registrationRepository.verifyEmail(emailVerifyEmailRequest, langCode)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({ response ->
                        if(response.error != null)
                            navigator?.handleError(Exception(response.error?.message))
                        else {
                            verifyEmailResponse.value = response
                            sessionId.value = response.sessionId
                            surveyQuestionId.value = response.surveyQuestionId
                            isVerifySuccess.value = true
                        }

                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        navigator?.handleError(ex)
                    }, {
                        setIsLoading(false)
                    }, {
                        setIsLoading(true)
                    }
                    )
            )

    }

    fun getDestinations() {
        compositeDisposable.add(
            registrationRepository.getDestinations()
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    destinations.value = response.response
                }, { ex ->
                    AppLogger.e(ex, "operation failed.")
                    setIsLoading(false)
                    navigator?.handleError(ex)
                }, {
                    setIsLoading(false)
                }, {
                    setIsLoading(true)
                }
                )
        )
    }

    fun destinationsUpdate(request: UpdatePersonnelCampusRequest) {
        compositeDisposable.add(
            registrationRepository.destinationsUpdate(request)
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    if(response.error != null)
                        navigator?.handleError(Exception(response.error?.message))
                    else {
                        isCampusUpdateSuccess.value = true
                    }
                }, { ex ->
                    AppLogger.e(ex, "operation failed.")
                    setIsLoading(false)
                    navigator?.handleError(ex)
                }, {
                    setIsLoading(false)
                }, {
                    setIsLoading(true)
                }
                )
        )
    }

}