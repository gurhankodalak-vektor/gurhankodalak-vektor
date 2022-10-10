package com.vektortelekom.android.vservice.ui.registration

import androidx.lifecycle.MutableLiveData
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.data.model.CheckDomainRequest
import com.vektortelekom.android.vservice.data.model.DestinationModel
import com.vektortelekom.android.vservice.data.model.EmailVerifyEmailRequest
import com.vektortelekom.android.vservice.data.model.RegisterVerifyCompanyCodeRequest
import com.vektortelekom.android.vservice.data.repository.RegistrationRepository
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import javax.inject.Inject

class RegistrationViewModel
@Inject
constructor(private val registrationRepository: RegistrationRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<BaseNavigator>() {


    val userName: MutableLiveData<String> = MutableLiveData()
    val userSurname: MutableLiveData<String> = MutableLiveData()
    val userEmail: MutableLiveData<String> = MutableLiveData()
    val companyAuthCode: MutableLiveData<String> = MutableLiveData()
    val userPassword: MutableLiveData<String> = MutableLiveData()

    val destinations: MutableLiveData<List<DestinationModel>> = MutableLiveData()

    val isVerifySuccess: MutableLiveData<Boolean> = MutableLiveData()

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
                        if(response.error != null) {
                            navigator?.handleError(Exception(response.error?.message))
                        }
                        else {
//                            isCompanyAuthCodeRequired.value = response.isCompanyAuthCodeRequired
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

    fun verifyEmail(emailVerifyEmailRequest: EmailVerifyEmailRequest, langCode: String) {

            compositeDisposable.add(
                registrationRepository.verifyEmail(emailVerifyEmailRequest, langCode)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({ response ->
                        if(response.error != null)
                            navigator?.handleError(Exception(response.error?.message))
                        else
                            isVerifySuccess.value = true

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

}