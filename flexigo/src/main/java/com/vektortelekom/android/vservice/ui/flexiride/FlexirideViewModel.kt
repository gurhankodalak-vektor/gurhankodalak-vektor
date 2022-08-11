package com.vektortelekom.android.vservice.ui.flexiride

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.repository.FlexirideRepository
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.data.repository.TicketRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.convertForBackend2
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject

class FlexirideViewModel
@Inject
constructor(
        private val ticketRepository: TicketRepository,
        private val flexirideRepository: FlexirideRepository,
        private val poolCarRepository: PoolCarRepository,
        private val userRepository: UserRepository,
        private val scheduler: SchedulerProvider): BaseViewModel<FlexirideNavigator>() {

    var isFrom = true

    var type: FlexirideCreateType = FlexirideCreateType.NORMAL

    val autocompletePredictions: MutableLiveData<List<AutocompletePrediction>> = MutableLiveData()

    val destinations: MutableLiveData<List<DestinationModel>> = MutableLiveData()

    val fromLocation : MutableLiveData<LatLng> = MutableLiveData()

    val toLocation : MutableLiveData<LatLng> = MutableLiveData()
    var shouldCameraNavigateTo: Boolean = false

    val passengerCountString : MutableLiveData<String> = MutableLiveData()
    val childSeatCountString : MutableLiveData<String> = MutableLiveData()

    var selectedDate: Date? = null

    val createFlexirideResponse: MutableLiveData<PoolcarAndFlexirideModel> = MutableLiveData()

    val flexirideList: MutableLiveData<List<PoolcarAndFlexirideModel>> = MutableLiveData()

    var addressTextFrom: String? = null
    var addressTextTo: String? = null

    var isFromAlreadySelected = false

    val startedFlexiride : MutableLiveData<PoolcarAndFlexirideModel> = MutableLiveData()

    val updatedFlexiride : MutableLiveData<PoolcarAndFlexirideModel> = MutableLiveData()

    val searchPersonResult: MutableLiveData<PersonnelModel?> = MutableLiveData()

    var personList = mutableListOf<PersonnelModel>()

    var passengerCount = 1

    val description: MutableLiveData<String> = MutableLiveData()

    val fromDescription: MutableLiveData<String> = MutableLiveData()

    val nameSurname: MutableLiveData<String> = MutableLiveData()
    val phoneNumber: MutableLiveData<String> = MutableLiveData()

    val flexirideReasons: MutableLiveData<List<String>> = MutableLiveData()
    var selectedReasonIndex : Int? = null
    var selectedReason: String? = null

    var evaluateFlexiride: PoolcarAndFlexirideModel? = null

    fun getDestinations() {
        compositeDisposable.add(
                ticketRepository.getDestinations()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            destinations.value = response.response
                        }, { ex ->
                            AppLogger.e(ex, "operation failed.")
                            setIsLoading(false)
                            when (ex) {
                                is HttpException -> {
                                    if (ex.response()?.code() == 403) {
                                        sessionExpireError.value = true
                                    } else {
                                        navigator?.handleError(ex)
                                    }
                                }
                                else -> {
                                    navigator?.handleError(ex)
                                }
                            }
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun createFlexiride(view: View?) {

        if(fromDescription.value.isNullOrBlank()) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.from_description_empty)))
            }
            return
        }
        else if(type == FlexirideCreateType.NORMAL && passengerCount != personList.size + 1) {
            navigator?.handleError(Exception(view?.context?.getString(R.string.flexiride_passenger_count_error)?:""))
            return
        }
        else if(selectedReason == null) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_flexiride_select_purpose)))
            }
            return
        }
        else if(description.value.isNullOrBlank()) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.description_empty)))
            }
            return
        }
        else if(type == FlexirideCreateType.GUEST && nameSurname.value.isNullOrBlank()) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.description_empty)))
            }
            return
        }
        else if(type == FlexirideCreateType.GUEST  && phoneNumber.value.isNullOrBlank()) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.description_empty)))
            }
            return
        }

        val requestType = if(type == FlexirideCreateType.GUEST) FlexirideAndPoolcarRequestType.GUESTRIDE else FlexirideAndPoolcarRequestType.FLEXIRIDE

        val request = PoolcarAndFlexirideModel(
                requestType = requestType,
                fromLocation = TaxiLocationModel(
                        fromLocation.value?.latitude?:0.0,
                        fromLocation.value?.longitude?:0.0,
                        addressTextFrom?:""
                ),
                toLocation = TaxiLocationModel(
                        toLocation.value?.latitude?:0.0,
                        toLocation.value?.longitude?:0.0,
                        addressTextTo?:""
                ),
                flexirideRequest = FlexirideRequestModel(
                        requestedPickupTime = (selectedDate?:Date()).convertForBackend2(),
                        passengerCount = passengerCountString.value?.toInt()?:1,
                        requiredChildSeats = childSeatCountString.value?.toInt()?:0,
                        fullName = nameSurname.value,
                        mobile = phoneNumber.value
                ),
                reservation = PoolcarReservationModel(
                        description = description.value?:"",
                        reason = selectedReason?:""
                ),
                fromLocationDescription = fromDescription.value
        )

        if(personList.isNotEmpty()) {
            val additionalRiders = mutableListOf<Int>()
            for(person in personList) {
                person.accountId?.let {
                    additionalRiders.add(it)
                }

            }

            if(additionalRiders.isNotEmpty()) {
                request.additionalRiders = additionalRiders
            }
        }


        compositeDisposable.add(
                flexirideRepository.createFlexiride(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            createFlexirideResponse.value = response
                        }, { ex ->
                            AppLogger.e(ex, "operation failed.")
                            setIsLoading(false)
                            when (ex) {
                                is HttpException -> {
                                    if (ex.response()?.code() == 403) {
                                        sessionExpireError.value = true
                                    } else {
                                        navigator?.handleError(ex)
                                    }
                                }
                                else -> {
                                    navigator?.handleError(ex)
                                }
                            }
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun getFlexirideList() {
        compositeDisposable.add(
                flexirideRepository.getFlexirideList()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            flexirideList.value = response
                        }, { ex ->
                            AppLogger.e(ex, "operation failed.")
                            setIsLoading(false)
                            when (ex) {
                                is HttpException -> {
                                    if (ex.response()?.code() == 403) {
                                        sessionExpireError.value = true
                                    } else {
                                        navigator?.handleError(ex)
                                    }
                                }
                                else -> {
                                    navigator?.handleError(ex)
                                }
                            }
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun updateFlexiride() {
        startedFlexiride.value?.id?.let {
            compositeDisposable.add(
                    flexirideRepository.getFlexiride(it)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                updatedFlexiride.value = response
                            }, { ex ->
                                AppLogger.e(ex, "operation failed.")
                            }, {
                            }, {
                            }
                            )
            )
        }
    }

    fun deleteFlexiride(id: Int) {
        compositeDisposable.add(
                flexirideRepository.deleteFlexiride(id)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({
                            getFlexirideList()
                        }, { ex ->
                            AppLogger.e(ex, "operation failed.")
                            setIsLoading(false)
                            when (ex) {
                                is HttpException -> {
                                    if (ex.response()?.code() == 403) {
                                        sessionExpireError.value = true
                                    } else {
                                        navigator?.handleError(ex)
                                    }
                                }
                                else -> {
                                    navigator?.handleError(ex)
                                }
                            }
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun searchPersonWithRegistrationNumber(registrationNumber: String) {
        compositeDisposable.add(
                userRepository.searchPersonWithRegistrationNumber(registrationNumber)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response.error != null) {
                                navigator?.handleError(Exception(response.error?.message))
                            }
                            else {
                                searchPersonResult.value = response.response
                            }
                        }, { ex ->
                            AppLogger.e(ex, "operation failed.")
                            setIsLoading(false)
                            when (ex) {
                                is HttpException -> {
                                    if (ex.response()?.code() == 403) {
                                        sessionExpireError.value = true
                                    } else {
                                        navigator?.handleError(ex)
                                    }
                                }
                                else -> {
                                    navigator?.handleError(ex)
                                }
                            }
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun getFlexirideReasons() {

        compositeDisposable.add(
                poolCarRepository.getReservationReasons()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            flexirideReasons.value = response
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            setIsLoading(false)
                            when (ex) {
                                is HttpException -> {
                                    if (ex.response()?.code() == 403) {
                                        sessionExpireError.value = true
                                    } else {
                                        navigator?.handleError(ex)
                                    }
                                }
                                else -> {
                                    navigator?.handleError(ex)
                                }
                            }
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    enum class FlexirideCreateType{
        NORMAL,
        GUEST
    }

}