package com.vektortelekom.android.vservice.ui.carpool

import androidx.lifecycle.MutableLiveData
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.repository.RegistrationRepository
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import javax.inject.Inject

class CarPoolViewModel
@Inject
constructor(private val registrationRepository: RegistrationRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<BaseNavigator>() {


    var userName: String = ""

    val destinationId: MutableLiveData<Long> = MutableLiveData()

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


}