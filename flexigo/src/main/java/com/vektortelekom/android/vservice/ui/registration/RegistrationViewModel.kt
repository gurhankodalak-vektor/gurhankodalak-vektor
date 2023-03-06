package com.vektortelekom.android.vservice.ui.registration

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.vektor.ktx.data.remote.model.BaseErrorModel
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektor.vshare_api_ktx.model.MobileParameters
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.repository.RegistrationRepository
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import retrofit2.HttpException
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
    val surveyQuestionId: MutableLiveData<Int?> = MutableLiveData()

    val destinationId: MutableLiveData<Long> = MutableLiveData()
    val verifyEmailResponse: MutableLiveData<VerifyEmailResponse> = MutableLiveData()

    val isCompanyAuthCodeRequired: MutableLiveData<Boolean> = MutableLiveData()

    fun checkDomain(checkDomainRequest: CheckDomainRequest, langCode: String, isFirstTry: Boolean) {

            compositeDisposable.add(
                registrationRepository.checkDomain(checkDomainRequest, langCode)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({ response ->
                        if(response.error != null) {
                            navigator?.handleError(Exception(response.error?.message))
                        }
                        else {
                            AppDataManager.instance.isShowLanding = true
                            isCompanyAuthCodeRequired.value = response.isCompanyAuthCodeRequired
                        }
                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        if (isFirstTry && this.getErrorIdFromHTTPException(ex) == 205) {
                            navigator?.tryCheckDomainWithOtherServer(checkDomainRequest, langCode)
                        }
                        else {
                            navigator?.handleError(ex)
                        }
                    }, {
                        setIsLoading(false)
                    }, {
                        setIsLoading(true)
                    }
                    )
            )

    }

    fun sendCompanyCode(registerVerifyCompanyCodeRequest: RegisterVerifyCompanyCodeRequest, langCode: String, isFirstTry: Boolean) {

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
                        if (isFirstTry && this.getErrorIdFromHTTPException(ex) == 199) {
                            navigator?.tryCompanyCodeWithOtherServer(registerVerifyCompanyCodeRequest, langCode)
                        }
                        else {
                            navigator?.handleError(ex)
                        }
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
                        if(response.error != null) {
                            navigator?.handleError(Exception(response.error?.message))
                        } else {
                            surveyQuestionId.value = response.surveyQuestionId
                            verifyEmailResponse.value = response
                            sessionId.value = response.sessionId
                            getMobileParameters()
                            isVerifySuccess.value = true
                        }

                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        isVerifySuccess.value = false
                    }, {
                        setIsLoading(false)
                    }, {
                        setIsLoading(true)
                    }
                    )
            )

    }

    fun getMobileParameters() {
        compositeDisposable.add(
            registrationRepository.getMobileParameters()
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    val mobileParameters = MobileParameters(response)
                    AppDataManager.instance.mobileParameters = mobileParameters
                    AppDataManager.instance.mobileParameters.longNearbyStationDurationInMin
                }, { ex ->
                    setIsLoading(false)
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
                        AppDataManager.instance.personnelInfo = response.response
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