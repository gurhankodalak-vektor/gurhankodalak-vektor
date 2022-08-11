package com.vektortelekom.android.vservice.ui.poolcar.reservation

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.vektor.ktx.utils.logger.AppLogger
import com.vektor.vshare_api_ktx.model.PriceModel2
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.ui.poolcar.reservation.adapter.PoiListAdapter
import com.vektortelekom.android.vservice.utils.convertForBackend2
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject

class PoolCarReservationViewModel
@Inject
constructor(private val poolCarRepository: PoolCarRepository,
            private val userRepository: UserRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<PoolCarReservationNavigator>() {

    val reservations: MutableLiveData<List<PoolcarAndFlexirideModel>> = MutableLiveData()

    val description: MutableLiveData<String> = MutableLiveData()
    val dutyLocations: MutableLiveData<String> = MutableLiveData()

    val stations : MutableLiveData<List<ParkModel>> = MutableLiveData()
    var selectedParkIndex : Int? = null
    var selectedPark: ParkModel? = null

    val reservationReasons: MutableLiveData<List<String>> = MutableLiveData()
    var selectedReasonIndex : Int? = null
    var selectedReason: String? = null

    var selectedStartDate: Date? = null
    var selectedEndDate: Date? = null

    val reservationAddResponse: MutableLiveData<PoolcarAndFlexirideModel> = MutableLiveData()

    val cancelReservationSuccess: MutableLiveData<Boolean> = MutableLiveData()

    val reservationToLocationTemp: MutableLiveData<LatLng> = MutableLiveData()
    val reservationAddressTextToTemp: MutableLiveData<String> = MutableLiveData()

    val reservationToLocation: MutableLiveData<LatLng> = MutableLiveData()
    val reservationAddressTextTo: MutableLiveData<String> = MutableLiveData()

    val searchPersonResult: MutableLiveData<PersonnelModel> = MutableLiveData()

    val personList: MutableList<PersonnelModel> = mutableListOf()

    val priceModels: MutableLiveData<List<PriceModel2>> = MutableLiveData()
    var selectedPriceModelIndex : Int? = null
    var selectedPriceModel: PriceModel2? = null

    var isIntercity: Boolean = false

    var isSelectVehicleFromOrTo = true

    val poiList: MutableLiveData<List<PoiModel>> = MutableLiveData()

    var selectedPoiFrom: MutableLiveData<PoiListAdapter.PoiListItem> = MutableLiveData()
    var selectedPoiTo: MutableLiveData<PoiListAdapter.PoiListItem> = MutableLiveData()

    var selectedReservationToStart: MutableLiveData<PoolcarAndFlexirideModel> = MutableLiveData()

    var reservationIdToUpdateWithQr: Int? = null

    fun getReservations() {
        compositeDisposable.add(
                poolCarRepository.getReservations()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            reservations.value = response
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            setIsLoading(false)
                            when (ex) {
                                is HttpException -> {
                                    if (ex.response()?.code() == 403) {
                                        sessionExpireError.value = true
                                        navigator?.showUnauthorizedMessage()
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

    fun getStations() {

        compositeDisposable.add(
                poolCarRepository.getStations()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            stations.value = response
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

    fun getReservationReasons() {

        compositeDisposable.add(
                poolCarRepository.getReservationReasons()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            reservationReasons.value = response
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

    fun submitReservation(view: View?) {

        if(isIntercity.not() && selectedPark == null) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_reservation_select_park)))
            }
        }
        else if(isIntercity.not() && reservationToLocation.value == null && BuildConfig.FLAVOR == "tums") {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_reservation_select_to)))
            }
        }
        else if(isIntercity && selectedPoiFrom.value == null) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_reservation_select_from)))
            }
        }
        else if(isIntercity && selectedPoiTo.value == null) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_reservation_select_to)))
            }
        }
        else if(isIntercity && dutyLocations.value.isNullOrBlank()) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.duty_locations_empty)))
            }
        }
        else if(selectedReason == null) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_reservation_select_purpose)))
            }
        }
        else if(selectedStartDate == null) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_reservation_select_start_date)))
            }
        }
        else if(selectedEndDate == null){
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_reservation_select_finish_date)))
            }
        }
        else if(description.value.isNullOrBlank()) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.description_empty)))
            }
        }
        else if((selectedEndDate?:Date()).time - (selectedStartDate?:Date()).time < 1000*60*30) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.description_empty)))
            }
        }
        else if(selectedPriceModel == null) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.vehicle_price_model_empty)))
            }
        }
        else {

            val poiFrom = selectedPoiFrom.value
            val poiTo = selectedPoiTo.value

            val request = PoolcarAndFlexirideModel(
                    requestType = if(isIntercity) FlexirideAndPoolcarRequestType.PARTNER_CAR_REQUEST else FlexirideAndPoolcarRequestType.POOL_CAR,
                    flexirideRequest = FlexirideRequestModel(
                            priceModelId = selectedPriceModel?.id?:0,
                            destinationsText = dutyLocations.value,
                            requestedPickupTime = if(isIntercity) selectedStartDate.convertForBackend2() else null,
                            requestedDeliveryTime = if(isIntercity) selectedEndDate.convertForBackend2() else null
                    ),
                    reservation = PoolcarReservationModel(
                            startTime = if(isIntercity) null else selectedStartDate.convertForBackend2(),
                            endTime = if(isIntercity) null else selectedEndDate.convertForBackend2(),
                            description = description.value?:"",
                            reason = selectedReason?:""
                    ),
                    travelDestinationType = if(isIntercity) TravelDestinationType.INTERCITY else TravelDestinationType.LOCAL
            )

            if(isIntercity) {
                request.flexirideRequest?.startPoiId = if(poiFrom is PoiModel) poiFrom.id else if (poiFrom is ParkModel) poiFrom.poiId else null
                request.flexirideRequest?.endPoiId = if(poiTo is PoiModel) poiTo.id else if (poiTo is ParkModel) poiTo.poiId else null
            }
            else {
                request.park = selectedPark
                request.toLocation = TaxiLocationModel(
                        reservationToLocation.value?.latitude?:0.0,
                        reservationToLocation.value?.longitude?:0.0,
                        reservationAddressTextTo.value?:""
                )
            }

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
                    poolCarRepository.addReservation(request)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ addResponse ->

                                reservationAddResponse.value = addResponse

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

    }

    fun cancelReservation(id: Int) {

        compositeDisposable.add(
                poolCarRepository.cancelReservation(id)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({
                            cancelReservationSuccess.value = true
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

    /*fun getStationVehicles(id: Int) {

        compositeDisposable.add(
                poolCarRepository.getStationVehicles(id)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            stationVehicles.value = response
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
    }*/

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
                                searchPersonResult.value = response.response!!
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

    fun availablePriceModels() {

        val request = PoolcarAndFlexirideModel(
                reservation = PoolcarReservationModel(
                        startTime = selectedStartDate.convertForBackend2(),
                        endTime = selectedEndDate.convertForBackend2()
                ),
                travelDestinationType = if(isIntercity) TravelDestinationType.INTERCITY else TravelDestinationType.LOCAL
        )

        if(isIntercity) {
            val poiFrom = selectedPoiFrom.value
            val poiTo = selectedPoiTo.value

            if(poiFrom == null && poiTo == null) {
                return
            }

            request.flexirideRequest = FlexirideRequestModel(
                    startPoiId = if(poiFrom is PoiModel) poiFrom.id else if (poiFrom is ParkModel) poiFrom.poiId else null,
                    endPoiId = if(poiTo is PoiModel) poiTo.id else if (poiTo is ParkModel) poiTo.poiId else null
            )

            request.requestType = FlexirideAndPoolcarRequestType.PARTNER_CAR_REQUEST

        }
        else {
            if(selectedPark?.id == null) {
                return
            }
            else {
                request.park = ParkModel(id = selectedPark?.id!!)
            }

        }

        compositeDisposable.add(
                poolCarRepository.availablePriceModels(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            priceModels.value = response

                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            priceModels.value = listOf()
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

    fun getPoiList() {
        compositeDisposable.add(
                poolCarRepository.getPoiList()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            poiList.value = response
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

    fun updateReservationVehicleWithQr(qrCode: String) {

        reservationIdToUpdateWithQr?.let { id ->

            compositeDisposable.add(
                    poolCarRepository.updateReservationVehicleWithQr(id, qrCode)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                selectedReservationToStart.value = response
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


    }

}