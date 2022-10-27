package com.vektortelekom.android.vservice.ui.carpool

import androidx.lifecycle.MutableLiveData
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.repository.CarPoolRepository
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import javax.inject.Inject

class CarPoolViewModel
@Inject
constructor(private val carPoolRepository: CarPoolRepository,
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

    val arrivalHourPopup: MutableLiveData<Int> = MutableLiveData()
    val departureHourPopup: MutableLiveData<Int> = MutableLiveData()

    val viewPagerCurrentItem: MutableLiveData<Int> = MutableLiveData()

    fun getCarpool() {
//        setIsLoading(false)
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

            compositeDisposable.add(
                carPoolRepository.updateCarPoolPreferences(request)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({
                        getCarpool()
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

    fun setChooseDriver(request: ChooseDriverRequest) {

            compositeDisposable.add(
                carPoolRepository.setChooseDriver(request)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({
                        getCarpool()
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

    fun setChooseRider(request: ChooseRiderRequest) {

            compositeDisposable.add(
                carPoolRepository.setChooseRider(request)
                    .observeOn(scheduler.ui())
                    .subscribeOn(scheduler.io())
                    .subscribe({
                        getCarpool()
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