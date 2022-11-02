package com.vektortelekom.android.vservice.ui.carpool

import androidx.lifecycle.MutableLiveData
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.repository.CarPoolRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import javax.inject.Inject

class CarPoolViewModel
@Inject
constructor(private val carPoolRepository: CarPoolRepository,
            private val userRepository: UserRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<BaseNavigator>() {

    val carPoolResponse: MutableLiveData<CarPoolResponse> = MutableLiveData()
    val closeDrivers: MutableLiveData<ArrayList<CarPoolListModel>> = MutableLiveData()
    val closeRiders: MutableLiveData<ArrayList<CarPoolListModel>> = MutableLiveData()
    val matchedDrivers: MutableLiveData<List<CarPoolListModel>> = MutableLiveData()
    val matchedRiders: MutableLiveData<List<CarPoolListModel>> = MutableLiveData()
    val ridingWith: MutableLiveData<CarPoolListModel> = MutableLiveData()
    val approvedRiders: MutableLiveData<List<CarPoolListModel>> = MutableLiveData()
    val carPoolPreferences: MutableLiveData<CarPoolPreferencesModel> = MutableLiveData()

    val isDriver: MutableLiveData<Boolean> = MutableLiveData()
    val isRider: MutableLiveData<Boolean> = MutableLiveData()

    val arrivalHour: MutableLiveData<Int> = MutableLiveData()
    val departureHour: MutableLiveData<Int> = MutableLiveData()

    val arrivalHourPopup: MutableLiveData<String> = MutableLiveData()
    val departureHourPopup: MutableLiveData<String> = MutableLiveData()

    val viewPagerCurrentItem: MutableLiveData<Int> = MutableLiveData()
    val phoneNumber: MutableLiveData<String> = MutableLiveData()

    val areaCode: MutableLiveData<String> = MutableLiveData()

    val countryCode: MutableLiveData<List<CountryCodeResponseListModel>> = MutableLiveData()
    val isUpdatedPhoneNumber: MutableLiveData<Boolean> = MutableLiveData()
    val isUpdatedOtp: MutableLiveData<Boolean> = MutableLiveData()

    val qrCodeResponse: MutableLiveData<String> = MutableLiveData()
    val isReadQrCode: MutableLiveData<Boolean> = MutableLiveData()

    fun getCarpool(isRefresh: Boolean) {
        if (isRefresh){
            setIsLoading(false)
        }
            compositeDisposable.add(
                carPoolRepository.getCarpool()
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({ response ->
                        if(response.error != null) {
                            navigator?.handleError(Exception(response.error?.message))
                        }
                        else {
                            carPoolResponse.value = response
                            closeDrivers.value = response.response.closeDrivers
                            closeRiders.value = response.response.closeRiders
                            matchedDrivers.value = response.response.matchedDrivers
                            matchedRiders.value = response.response.matchedRiders
                            carPoolPreferences.value = response.response.carPoolPreferences
                            approvedRiders.value = response.response.approvedRiders
                            ridingWith.value = response.response.ridingWith
                            isDriver.value = response.response.carPoolPreferences?.isDriver
                            isRider.value = response.response.carPoolPreferences?.isRider
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

    fun updateCarPoolPreferences(request: CarPoolPreferencesRequest) {
        setIsLoading(false)
            compositeDisposable.add(
                carPoolRepository.updateCarPoolPreferences(request)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({
                        getCarpool(true)
                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        navigator?.handleError(ex)
                    }, {
                        setIsLoading(false)
                    }, {
                    }
                    )
            )
    }

    fun setChooseDriver(request: ChooseDriverRequest) {
        setIsLoading(false)
            compositeDisposable.add(
                carPoolRepository.setChooseDriver(request)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({
                        getCarpool(true)
                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        navigator?.handleError(ex)
                    }, {
                        setIsLoading(false)
                    }, {
                    }
                    )
            )
    }

    fun setChooseRider(request: ChooseRiderRequest) {
        setIsLoading(false)
            compositeDisposable.add(
                carPoolRepository.setChooseRider(request)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({
                        getCarpool(true)
                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        navigator?.handleError(ex)
                    }, {
                        setIsLoading(false)
                    }, {
                    }
                    )
            )
    }

    fun getCountryCode() {
            compositeDisposable.add(
                carPoolRepository.getCountryCode()
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({ response ->
                        countryCode.value = response.en
                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        navigator?.handleError(ex)
                    }, {
                        setIsLoading(false)
                    }, {
                    }
                    )
            )
    }

    fun sendPhoneNumber(request: InfoUpdateRequest) {
            compositeDisposable.add(
                carPoolRepository.sendPhoneNumber(request)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({ response ->
                        if(response.error != null)
                            navigator?.handleError(Exception(response.error?.message))
                        else
                            isUpdatedPhoneNumber.value = true
                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        navigator?.handleError(ex)
                    }, {
                        setIsLoading(false)
                    }, {
                    }
                    )
            )
    }

    fun sendOtpCode(request: SendOtpRequest) {
            compositeDisposable.add(
                carPoolRepository.sendOtpCode(request)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({ response ->
                        if(response.error != null)
                            navigator?.handleError(Exception(response.error?.message))
                        else {
                            isUpdatedOtp.value = true
                            getPersonnelInfo()
                        }
                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        navigator?.handleError(ex)
                    }, {
                        setIsLoading(false)
                    }, {
                    }
                    )
            )
    }

    fun readQrCode(value: ResponseModel) {
            compositeDisposable.add(
                carPoolRepository.sendQrCode(value)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({ response ->
                        if(response.error != null)
                            navigator?.handleError(Exception(response.error?.message))
                        else {
                            isReadQrCode.value = true
                        }
                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        navigator?.handleError(ex)
                    }, {
                        setIsLoading(false)
                    }, {
                    }
                    )
            )
    }

    fun getQrCode() {
            compositeDisposable.add(
                carPoolRepository.getMyQrCode()
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({ response ->
                        qrCodeResponse.value = response?.response
                    }, { ex ->
                        println("error: ${ex.localizedMessage}")
                        setIsLoading(false)
                        navigator?.handleError(ex)
                    }, {
                        setIsLoading(false)
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
                    if (response.error != null) {
                        navigator?.showLoginActivity()
                    } else {
                        AppDataManager.instance.personnelInfo = response.response
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