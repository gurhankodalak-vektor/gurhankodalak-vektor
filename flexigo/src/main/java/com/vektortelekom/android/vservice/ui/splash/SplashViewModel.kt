package com.vektortelekom.android.vservice.ui.splash

import androidx.lifecycle.MutableLiveData
import com.vektor.vshare_api_ktx.model.MobileParameters
import com.vektor.vshare_api_ktx.model.VersionV2Response
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.PersonelInfoResponse
import com.vektortelekom.android.vservice.data.repository.MobileRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import javax.inject.Inject

class SplashViewModel
@Inject
constructor(private val mobileRepository: MobileRepository,
            private val userRepository: UserRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<SplashNavigator>() {

    val checkVersionResponse: MutableLiveData<VersionV2Response> = MutableLiveData()
    val personnelDetailsResponse: MutableLiveData<PersonelInfoResponse> = MutableLiveData()
    var isCommuteOptionsEnabled: Boolean = false

    fun checkVersion(appName: String, platform: String) {
        compositeDisposable.add(
                mobileRepository.checkVersion(appName, platform)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            checkVersionResponse.value = response
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            navigator?.handleError(ex)
                        }, {
                        }, {
                        }
                        )
        )
    }

    fun getPersonnelInfo() {
        compositeDisposable.add(
                userRepository.getPersonalDetails()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response.error != null) {
                                navigator?.showLoginActivity()
                            }
                            else {
                                personnelDetailsResponse.value = response
                            }
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            navigator?.handleError(ex)
                        }, {
                        }, {
                        }
                        )
        )
    }

    fun getMobileParameters() {
        compositeDisposable.add(
                userRepository.getMobileParameters()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            val mobileParameters = MobileParameters(response)
                            AppDataManager.instance.mobileParameters = mobileParameters
                            AppDataManager.instance.mobileParameters.longNearbyStationDurationInMin
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun updateFirebaseToken(firebaseToken: String) {
        compositeDisposable.add(
                userRepository.updateFirebaseToken(firebaseToken)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({
                            getCompanySettings()
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            navigator?.handleError(ex)
                        }, {
                        }, {
                        }
                        )
        )
    }

    private fun getCompanySettings() {
        compositeDisposable.add(
                userRepository.companySettings()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            AppDataManager.instance.commuteOptionsEnabled = response.isCommuteOptionsEnabled ?: false
                            isCommuteOptionsEnabled = response.isCommuteOptionsEnabled ?: false
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            navigator?.handleError(ex)
                        }, {
                        }, {
                        }
                        )
        )
    }

}