package com.vektortelekom.android.vservice.ui.poolcar

import android.content.Context
import android.location.Location
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.vektor.ktx.utils.logger.AppLogger
import com.vektor.vshare_api_ktx.model.*
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.model.CustomerStatusModel
import com.vektortelekom.android.vservice.data.model.RentalModel
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import java.lang.Exception
import java.util.ArrayList
import javax.inject.Inject

class PoolCarViewModel
@Inject
constructor(private val poolCarRepository: PoolCarRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<PoolCarNavigator>() {

    val stations : MutableLiveData<List<ParkModel>> = MutableLiveData()

    val selectedStation : MutableLiveData<ParkModel> = MutableLiveData()

    val stationVehicles: MutableLiveData<List<StationVehicleModel>> = MutableLiveData()

    val selectedVehicle: MutableLiveData<PoolCarVehicleModel> = MutableLiveData()

    var selectedVehicleImageUuid: String? = null

    var myLocation: Location? = null

    val customerStatus: MutableLiveData<CustomerStatusModel> = MutableLiveData()

    val customerStatusReservation: MutableLiveData<CustomerStatusModel> = MutableLiveData()

    val createRentalResponse: MutableLiveData<CreateRentalResponse> = MutableLiveData()

    val cancelRentalResponse: MutableLiveData<CreateRentalResponse> = MutableLiveData()

    val finishRentalResponse: MutableLiveData<CreateRentalResponse> = MutableLiveData()

    val startRentalResponse: MutableLiveData<CreateRentalResponse> = MutableLiveData()

    val rental: MutableLiveData<RentalModel> = MutableLiveData()

    val vehicleDamages: MutableLiveData<List<DamageModel>> = MutableLiveData()

    var finishParkInfoPreviewPhotoPath: String? = null

    var addNewDamagePreviewPhotoPath: String? = null

    var addNewInternalDamagePreviewPhotoPath: String? = null

    val isFinishParkInfoConfirmChecked: MutableLiveData<Boolean> = MutableLiveData(false)

    val finishParkInfoDescription : MutableLiveData<String> = MutableLiveData()

    val checkDoorResponse: MutableLiveData<CheckDoorResponse> = MutableLiveData()

    val isDamageAdded: MutableLiveData<Boolean> = MutableLiveData()

    var isPreviewExternal: Boolean? = null

    var isReservation = false

    var reservationId : Int? = null

    val rentalBillInfoResponse: MutableLiveData<BillInfoModel> = MutableLiveData()

    var startRentalOdometer: Double? = null
    var endRentalOdometer: Double? = null

    val isStartAgreementApproved: MutableLiveData<Boolean> = MutableLiveData(false)

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

    fun getStationVehicles(id: Int) {

        compositeDisposable.add(
                poolCarRepository.getStationVehicles(id)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            stationVehicles.value = response
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

    fun getCustomerStatus() {

        compositeDisposable.add(
                poolCarRepository.getCustomerStatus()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            customerStatus.value = response
                            if(response.rental != null) {
                                rental.value = response.rental
                                selectedVehicle.value = response.vehicle
                            }
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

    fun getCustomerStatusForReservation() {

        compositeDisposable.add(
                poolCarRepository.getCustomerStatus()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            customerStatusReservation.value = response
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

    fun createRental(view: View?) {

        if(view == null) {
            return
        }

        if(isStartAgreementApproved.value != true) {
            FlexigoInfoDialog.Builder(view.context)
                    .setIconVisibility(false)
                    .setCancelable(false)
                    .setText1(view.context.getString(R.string.rental_start_checkbox_unchecked_error_text))
                    .setOkButton(view.context.getString(R.string.Generic_Ok)) {
                        it.dismiss()
                    }
                    .create()
                    .show()

            return
        }

        val request = RentalCreateRequest2()

        if(selectedStation.value == null) {
            view.let {
                navigator?.handleError(Exception(view.context.getString(R.string.error_station)))
            }
            return
        }
        else {
            request.parkId = selectedStation.value!!.id
        }

        if(selectedVehicle.value == null) {
            view.let {
                navigator?.handleError(Exception(view.context.getString(R.string.error_vehicle)))
            }
            return
        }
        else {

            selectedVehicle.value?.let { vehicle ->

                if(isReservation) {
                    request.vehicleId = vehicle.id
                    request.reservationId = reservationId
                }
                else {
                    request.vehicleCatalogId = vehicle.id
                }

                request.priceModelId = vehicle.priceModelId

                if(vehicle.location == null) {
                    val dam = DeliveryAddressModel2()
                    dam.latitude = 41.0
                    dam.longitude = 29.0
                    request.deliveryLocation = dam
                }
                else {
                    request.deliveryLocation = vehicle.location
                }
            }


        }

        myLocation?.let { location ->
            val dam = DeliveryAddressModel()
            dam.latitude = location.latitude
            dam.longitude = location.longitude
            request.customerLocation = dam
        }

        request.rentalType = RentalType.POOL

        customerStatus.value?.user?.corporateOrganizationId?.let { request.organizationId = it }
                ?: customerStatusReservation.value?.user?.corporateOrganizationId?.let { request.organizationId = it }

        if(request.organizationId == null) {
            view.let {
                navigator?.handleError(Exception(view.context.getString(R.string.error_organization)))
            }
            return
        }

        compositeDisposable.add(
                poolCarRepository.createRental(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            createRentalResponse.value = response
                            rental.value = response.rental
                            selectedVehicle.value = response.vehicle
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

    fun cancelRental() {

        rental.value?.id?.let { rentalId ->

            val request = RentalEndRequest()
            request.rentalId = rentalId.toLong()

            compositeDisposable.add(
                    poolCarRepository.cancelRental(request)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                cancelRentalResponse.value = response
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

        } ?: return
    }

    fun startRental(context: Context) {

        val deviceType = selectedVehicle.value?.deviceType

        if(deviceType == null) {
            navigator?.handleError(Exception(context.getString(R.string.error_vehicle_info)))
            return
        }

        if(deviceType == DeviceType.NONE && startRentalOdometer == null) {
            navigator?.handleError(Exception(context.getString(R.string.odometer_does_not_exists)))
            return
        }

        val request = StartRentalRequest(startRentalOdometer)

        compositeDisposable.add(
                poolCarRepository.startRental(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            startRentalResponse.value = response
                            rental.value = response.rental
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

    fun getVehicleDamages() {

        selectedVehicle.value?.id?.let {
            compositeDisposable.add(
                    poolCarRepository.getVehicleDamages(it)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                vehicleDamages.value = response
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

    fun getRentalDamages() {

        rental.value?.id?.let {
            compositeDisposable.add(
                    poolCarRepository.getRentalDamages(it)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({
                                //startRentalResponse.value = response
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

    fun finishRentalControl(view: View?) {

        if(BuildConfig.FLAVOR != "tums"
                && (finishParkInfoPreviewPhotoPath == null
                || finishParkInfoDescription.value.isNullOrEmpty()
                || isFinishParkInfoConfirmChecked.value != true)) {
            navigator?.handleError(Exception(view?.context?.getString(R.string.rental_finish_fields_empty)))
            return
        }

        navigator?.finishRental(view)

    }

    fun finishRental(context: Context) {

        if(BuildConfig.FLAVOR != "tums"
                && (finishParkInfoPreviewPhotoPath == null
                || finishParkInfoDescription.value.isNullOrEmpty()
                || isFinishParkInfoConfirmChecked.value != true)) {
            navigator?.handleError(Exception(context.getString(R.string.rental_finish_fields_empty)))
            return
        }

        val deviceType = selectedVehicle.value?.deviceType

        if(deviceType == null) {
            navigator?.handleError(Exception(context.getString(R.string.error_vehicle_info)))
            return
        }

        if(deviceType == DeviceType.NONE && endRentalOdometer == null) {
            navigator?.handleError(Exception(context.getString(R.string.odometer_does_not_exists)))
            return
        }

        rental.value?.let {

            if(BuildConfig.FLAVOR == "tums") {
                if(finishParkInfoPreviewPhotoPath == null) {
                    continueFinishRental(null)
                }
                else {
                    uploadPhotoForFinishRental()
                }
            }
            else {
                uploadPhotoForFinishRental()
            }

        }

    }

    private fun uploadPhotoForFinishRental() {
        val parts = ArrayList<MultipartBody.Part>()
        parts.add(MultipartBody.Part.createFormData("file1", "file1" + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), File(finishParkInfoPreviewPhotoPath!!))))

        compositeDisposable.add(
                poolCarRepository.uploadImages(parts)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            if(response.response?.fileUuids?.isEmpty()?.not() == true) {
                                continueFinishRental(response.response?.fileUuids!![0])
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

    private fun continueFinishRental(fileUuid: String?) {
        rental.value?.let { rental ->

            val request = RentalEndRequest()

            request.rentalId = rental.id.toLong()
            request.endLocationDescription = finishParkInfoDescription.value
            request.endLocationPhotoUuid = fileUuid
            request.endOdometer = endRentalOdometer

            compositeDisposable.add(
                    poolCarRepository.rentalEnd(request)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                finishRentalResponse.value = response
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

    fun addCarDamage(damage : DamageModel, shouldClose: Boolean) {

        rental.value?.id?.let {

            if(damage.fileUuids.isNullOrEmpty()) {
                return
            }

            val parts = ArrayList<MultipartBody.Part>()
            for (i in damage.fileUuids!!.indices) {
                parts.add(MultipartBody.Part.createFormData("file" + (i + 1), "file" + (i + 1) + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), File(damage.fileUuids!![i]))))
            }

            compositeDisposable.add(
                    poolCarRepository.uploadImages(parts)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                damage.fileUuids = response.response?.fileUuids
                                continueAddCarDamage(damage, shouldClose)

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

    private fun continueAddCarDamage(damage: DamageModel, shouldClose: Boolean) {

        rental.value?.id?.let { id ->

            val damageRequest = DamageRequest()

            damageRequest.rentalId = id.toLong()
            damageRequest.damage = damage

            compositeDisposable.add(
                    poolCarRepository.addCarDamage(damageRequest)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({
                                isDamageAdded.value = true
                                navigator?.damageAdded(damage, shouldClose)
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

    fun checkDoorStatus() {

        rental.value?.id?.let {
            compositeDisposable.add(
                    poolCarRepository.checkDoorStatus(it)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                checkDoorResponse.value = response
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
                                //setIsLoading(false)
                            }, {
                                //setIsLoading(true)
                            }
                            )
            )
        }
    }

    fun sendMultiRating(view: View?) {
        navigator?.rentalFinished()
    }

    fun getMobileParameters() {
        compositeDisposable.add(
                poolCarRepository.getMobileParameters()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            val mobileParameters = MobileParameters(response)
                            AppDataManager.instance.mobileParameters = mobileParameters
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

    fun getRentalBillInfo(rentalId: Long) {
        compositeDisposable.add(
                poolCarRepository.getRentalBillInfo(rentalId)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            rentalBillInfoResponse.value = response
                        }, { ex ->
                            AppLogger.e(ex, "operation failed.")
                            //setIsLoading(false)
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
                            //setIsLoading(false)
                        }, {
                            //setIsLoading(true)
                        }
                        )
        )
    }

}