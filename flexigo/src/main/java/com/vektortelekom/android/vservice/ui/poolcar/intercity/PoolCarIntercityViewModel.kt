package com.vektortelekom.android.vservice.ui.poolcar.intercity

import android.location.Location
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.PoolCarIntercityFinishRequest
import com.vektortelekom.android.vservice.data.model.PoolCarIntercityStartRequest
import com.vektortelekom.android.vservice.data.model.PoolcarAndFlexirideModel
import com.vektortelekom.android.vservice.data.model.TaxiLocationModel
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.ArrayList
import javax.inject.Inject

class PoolCarIntercityViewModel
@Inject
constructor(private val poolCarRepository: PoolCarRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<PoolCarIntercityNavigator>(){

    var id: Int? = null

    val licensePlate: MutableLiveData<String> = MutableLiveData()
    val startKm: MutableLiveData<String> = MutableLiveData()
    val vehicleMake: MutableLiveData<String> = MutableLiveData()
    val vehicleModel: MutableLiveData<String> = MutableLiveData()

    var location: Location? = null

    val finishKm: MutableLiveData<String> = MutableLiveData()
    val finishPhotoUuid: MutableLiveData<String> = MutableLiveData()

    val createRentalResponse: MutableLiveData<PoolcarAndFlexirideModel> = MutableLiveData()
    val finishRentalResponse: MutableLiveData<PoolcarAndFlexirideModel> = MutableLiveData()

    val fromLocation: MutableLiveData<String> = MutableLiveData()
    val finishDate: MutableLiveData<String> = MutableLiveData()
    val vehicleInfo: MutableLiveData<String> = MutableLiveData()

    val isStartAgreementApproved: MutableLiveData<Boolean> = MutableLiveData(false)

    fun startRental(view: View?) {

        if(id == null) {
            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.pool_car_intercity_start_error_id)))
            }
            return
        }
        else if(licensePlate.value.isNullOrBlank()) {
            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.pool_car_intercity_start_error_plate)))
            }
            return
        }
        else if(startKm.value == null) {
            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.pool_car_intercity_start_error_start_km)))
            }
            return
        }
        else if(vehicleMake.value.isNullOrBlank()) {
            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.pool_car_intercity_start_error_make)))
            }
            return
        }
        else if(vehicleModel.value.isNullOrBlank()) {
            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.pool_car_intercity_start_error_model)))
            }
            return
        }
        else if(location == null) {
            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.pool_car_intercity_start_error_location)))
            }
            return
        }

        val request = PoolCarIntercityStartRequest(
                plate = licensePlate.value?:"",
                startOdometer = startKm.value?.toDouble()?:0.0,
                make = vehicleMake.value?:"",
                model = vehicleModel.value?:"",
                startLocation = TaxiLocationModel(
                        latitude = location?.latitude?:0.0,
                        longitude = location?.longitude?:0.0,
                        address = ""
                )

        )

        compositeDisposable.add(
                poolCarRepository.startIntercityRental(id?:0, request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            createRentalResponse.value = response
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

    fun finishRental(view: View?) {

        if(id == null) {
            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.pool_car_intercity_start_error_id)))
            }
            return
        }
        else if(finishKm.value == null) {
            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.pool_car_intercity_start_error_finish_km)))
            }
            return
        }
        else if(finishPhotoUuid.value.isNullOrBlank()) {
            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.pool_car_intercity_start_error_finish_photo)))
            }
            return
        }
        else if(location == null) {
            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.pool_car_intercity_start_error_location)))
            }
            return
        }

        val parts = ArrayList<MultipartBody.Part>()
        parts.add(MultipartBody.Part.createFormData("file1", "file1" + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), File(finishPhotoUuid.value!!))))

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
                            navigator?.handleError(ex)
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )

    }

    private fun continueFinishRental(uuid: String) {
        val request = PoolCarIntercityFinishRequest(
                endOdometer = finishKm.value?.toDouble()?:0.0,
                photoUuid = uuid,
                endLocation = TaxiLocationModel(
                        latitude = location?.latitude?:0.0,
                        longitude = location?.longitude?:0.0,
                        address = ""
                )

        )

        compositeDisposable.add(
                poolCarRepository.finishIntercityRental(id?:0, request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            finishRentalResponse.value = response
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